package com.example.jammoney.news.service;

import com.example.jammoney.financeTerm.dto.TermCreateDto;
import com.example.jammoney.financeTerm.entity.FinancialTerm;
import com.example.jammoney.financeTerm.service.FinanceTermService;
import com.example.jammoney.news.dto.EasyWordTranslationDto;
import com.example.jammoney.news.entity.EasyWordTranslation;
import com.example.jammoney.news.entity.News;
import com.example.jammoney.news.repository.EasyWordTranslationRepository;
import com.example.jammoney.news.repository.NewsRepository;
import com.example.jammoney.news.quiz.gpt.NewsGptApiClient;
import com.example.jammoney.user.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.jammoney.news.quiz.gpt.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EasyWordTranslationService {

    private final NewsRepository newsRepo;
    private final EasyWordTranslationRepository translationRepo;
    private final NewsGptApiClient gptClient;
    private final ObjectMapper mapper;
    private final FinanceTermService termService;

    @Transactional
    public List<EasyWordTranslationDto> generateTranslations(Long newsId) throws Exception {
        log.info("[STEP 0] generateTranslations 시작, newsId={}", newsId);
        News news = newsRepo.findById(newsId)
                .orElseThrow(() -> {
                    log.error("[STEP 1] 뉴스 없음, id={}", newsId);
                    return new IllegalArgumentException("뉴스가 없습니다. id=" + newsId);
                });

        log.info("[STEP 1.1] content length={}, preview='{}'",
                news.getContent() == null ? 0 : news.getContent().length(),
                news.getContent() == null ? ""
                        : news.getContent().substring(0, Math.min(100, news.getContent().length()))
        );

        List<EasyWordTranslation> existing = translationRepo.findByNewsId(newsId);
        log.info("[STEP 2] existing size={}", existing.size());
        if (!existing.isEmpty()) {
            log.info("[STEP 2] 기존 데이터 반환");
            return existing.stream()
                    .map(e -> new EasyWordTranslationDto(
                            newsId,
                            e.getOriginalWord(),
                            e.getTranslatedText(),
                            e.getExampleSentence()
                    ))
                    .toList();
        }

        List<ChatMessage> messages = List.of(
                new ChatMessage("system", """
        You are a financial terminology translator.
        아래 뉴스 본문에서 **금융·경제 관련 용어**만 골라내어,
        쉽고 정확한 한국어로 번역한 뒤 예문을 포함한 JSON 배열만 출력하세요.
        금융·경제 용어가 아닐 경우 절대 포함하지 마십시오.
        다른 설명이나 부가 텍스트는 전혀 포함하지 마십시오.

        출력 형식 예시:
        [
          {
            "originalWord": "대출",
            "translatedText": "은행에서 돈을 빌리는 것",
            "exampleSentence": "집을 사기 위해 은행에서 대출을 받았다."
          },
          {
            "originalWord": "금리",
            "translatedText": "돈을 빌린 대가로 내는 이자의 비율",
            "exampleSentence": "은행은 대출 금리를 연 3%로 책정했다."
          },
          {
            "originalWord": "환율",
            "translatedText": "서로 다른 통화 간 교환 비율",
            "exampleSentence": "원/달러 환율이 상승하면 수입 물가가 오를 수 있다."
          }
        ]
        """),
                new ChatMessage("user", """
        뉴스 본문:
        """ + news.getContent())
        );
        log.info("[STEP 3] messages prepared, count={}", messages.size());

        String raw = gptClient.callChatCompletionRaw(messages).block();
        log.info("[STEP 4] GPT raw response={}", raw);
        if (raw == null || raw.isBlank()) {
            log.error("[STEP 4] 빈 응답");
            throw new RuntimeException("GPT로부터 빈 응답을 받았습니다.");
        }

        List<EasyWordTranslationDto> dtos;
        try {
            dtos = mapper.readValue(raw.trim(),
                    new TypeReference<List<EasyWordTranslationDto>>() {});
        } catch (Exception ex) {
            log.error("[STEP 5] JSON 파싱 실패, raw={}", raw, ex);
            throw ex;
        }
        log.info("[STEP 5] 파싱된 dtos size={}", dtos.size());

        for (EasyWordTranslationDto dto : dtos) {
            log.info("[STEP 6] 저장 original='{}'", dto.getOriginalWord());
            translationRepo.save(EasyWordTranslation.builder()
                    .news(news)
                    .originalWord(dto.getOriginalWord())
                    .translatedText(dto.getTranslatedText())
                    .exampleSentence(dto.getExampleSentence())
                    .build());
        }
        log.info("[STEP 6] 저장 완료");

        return dtos;
    }

    @Transactional
    public void saveSingleTermToMyTerms(Long newsId,
                                        EasyWordTranslationDto dto,
                                        User user) {

        Long myCategoryId = termService.getMyTermsCategoryId();

        TermCreateDto termDto = new TermCreateDto();
        termDto.setCategoryId(myCategoryId);
        termDto.setTerm(dto.getOriginalWord());
        termDto.setDefinition(dto.getTranslatedText());
        termDto.setExampleSentences(List.of(dto.getExampleSentence()));

        Optional<FinancialTerm> existing = termService.findByTermAndCategory(
                dto.getOriginalWord(), myCategoryId
        );

        Long termId = existing
                .map(FinancialTerm::getId)
                .orElseGet(() -> termService.createNewsTerm(termDto));

        termService.bookmarkTerm(user, termId);
    }
}

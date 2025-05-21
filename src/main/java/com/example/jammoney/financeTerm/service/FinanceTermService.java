package com.example.jammoney.financeTerm.service;

import com.example.jammoney.financeTerm.dto.*;
import com.example.jammoney.financeTerm.entity.*;
import com.example.jammoney.financeTerm.repository.*;
import com.example.jammoney.pet.entity.Pet;
import com.example.jammoney.pet.repository.PetRepository;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceTermService {

    private final QuizSubmissionRepository quizSubmissionRepository;
    private final CategoryRepository categoryRepository;
    private final TermRepository termRepository;
    private final SavedTermRepository savedTermRepository;
    private final UserLearningRepository userLearningRepository;
    private final TermQuizRepository termQuizRepository;
    private final PetRepository petRepository;

    // 1. 카테고리 전체 조회
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream().map(c -> {
            CategoryDto dto = new CategoryDto();
            dto.setId(c.getId());
            dto.setCategory(c.getCategory());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<Integer> getAvailableDayIndexes(String categoryName) {
        FinancialCategory category = categoryRepository.findByCategory(categoryName)
                .orElseThrow(() -> new RuntimeException("카테고리 없음"));

        List<FinancialTerm> terms = termRepository.findByCategory(category);
        return terms.stream()
                .map(FinancialTerm::getDayIndex)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }


    // 2. 특정 카테고리 + DayIndex 단어 조회
    public List<TermDto> getTermsByCategoryAndDay(String categoryName, int dayIndex, User user) {
        FinancialCategory category = categoryRepository.findByCategory(categoryName)
                .orElseThrow(() -> new RuntimeException("카테고리 없음"));

        List<FinancialTerm> terms = termRepository.findByCategoryAndDayIndex(category, dayIndex);
        List<UserSavedTerm> savedTerms = savedTermRepository.findAllByUser(user);
        List<UserTermLearning> learnings = userLearningRepository.findByUserAndTermIn(user, terms);

        return terms.stream().map(term -> {
            TermDto dto = new TermDto();
            dto.setTermId(term.getId());
            dto.setTerm(term.getTerm());
            dto.setDefinition(term.getDefinition());

            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setId(category.getId());
            categoryDto.setCategory(category.getCategory());
            dto.setCategory(categoryDto);

            dto.setDayIndex(term.getDayIndex());
            dto.setExampleSentences(term.getExampleSentences());
            dto.setBookmarked(savedTerms.stream().anyMatch(s -> s.getTerm().getId().equals(term.getId())));
            dto.setLearned(learnings.stream().anyMatch(l -> l.getTerm().getId().equals(term.getId())));
            return dto;
        }).collect(Collectors.toList());
    }

    // 3-1. 퀴즈 목록 조회 (dayIndex 단위 전체)
    public List<TermQuizDto> getQuizzesByCategoryAndDay(String categoryName, int dayIndex) {
        FinancialCategory category = categoryRepository.findByCategory(categoryName)
                .orElseThrow(() -> new RuntimeException("카테고리 없음"));

        List<FinancialTerm> terms = termRepository.findByCategoryAndDayIndex(category, dayIndex);
        List<TermQuizDto> allQuizzes = new ArrayList<>();

        for (FinancialTerm term : terms) {
            List<FinancialTermQuiz> quizzes = termQuizRepository.findByTerm(term);
            for (FinancialTermQuiz quiz : quizzes) {
                TermQuizDto dto = new TermQuizDto();
                dto.setQuizId(quiz.getId());
                dto.setQuestion(quiz.getQuestion());
                dto.setChoices(quiz.getChoices());
                allQuizzes.add(dto);
            }
        }
        return allQuizzes;
    }


    @Transactional
    public List<QuizResultDto> submitQuizAnswer(User user, QuizSubmitDto submitDto) {
        List<QuizResultDto> results = new ArrayList<>();

        for (QuizAnswerDto answer : submitDto.getAnswers()) {
            FinancialTermQuiz quiz = termQuizRepository.findById(answer.getQuizId())
                    .orElseThrow(() -> new RuntimeException("퀴즈를 찾을 수 없습니다."));

            FinancialTerm term = quiz.getTerm();

            // ✅ String → int 변환
            int selectedIndex = Integer.parseInt(answer.getSelectedAnswer());
            boolean isCorrect = selectedIndex == quiz.getCorrectAnswer();

            // ✅ 제출 저장
            quizSubmissionRepository.save(QuizSubmission.builder()
                    .user(user)
                    .quiz(quiz)
                    .selectedAnswerIndex(selectedIndex)
                    .isCorrect(isCorrect)
                    .build());

            // ✅ 정답 여부 관계없이 학습률 증가
            if (!userLearningRepository.existsByUserAndTerm(user, term)) {
                userLearningRepository.save(UserTermLearning.builder()
                        .user(user)
                        .term(term)
                        .learned(true)
                        .build());
            }

            // ✅ 결과 DTO 설정
            QuizResultDto result = new QuizResultDto();
            result.setCorrect(isCorrect);
            result.setCorrectAnswer(quiz.getCorrectAnswer());
            result.setSelectedAnswer(selectedIndex);
            results.add(result);
        }

        // ✅ 경험치 부여 조건
        FinancialCategory category = categoryRepository.findByCategory(submitDto.getCategoryName())
                .orElseThrow(() -> new RuntimeException("카테고리 없음"));

        List<FinancialTerm> terms = termRepository.findByCategoryAndDayIndex(category, submitDto.getDayIndex());
        List<FinancialTermQuiz> allQuizzes = terms.stream()
                .flatMap(t -> termQuizRepository.findByTerm(t).stream())
                .toList();

        List<QuizSubmission> userSubmissions = quizSubmissionRepository.findByUser(user);
        long solvedCount = allQuizzes.stream()
                .filter(q -> userSubmissions.stream().anyMatch(s -> s.getQuiz().getId().equals(q.getId())))
                .count();

        if (solvedCount == allQuizzes.size()) {
            Pet pet = petRepository.findByUserId(user.getId());
            if (pet == null) throw new RuntimeException("펫 정보가 없습니다.");
            pet.setExp(pet.getExp() + 5);
        }

        return results;
    }



    // 5. 즐겨찾기 등록
    public void bookmarkTerm(User user, Long termId) {
        FinancialTerm term = termRepository.findById(termId)
                .orElseThrow(() -> new RuntimeException("단어 없음"));

        boolean alreadyBookmarked = savedTermRepository.findByUserAndTerm(user, term).isPresent();
        if (!alreadyBookmarked) {
            UserSavedTerm saved = new UserSavedTerm();
            saved.setUser(user);
            saved.setTerm(term);
            savedTermRepository.save(saved);
        }
    }


    // 6. 즐겨찾기 해제
    public void unbookmarkTerm(User user, Long termId) {
        FinancialTerm term = termRepository.findById(termId)
                .orElseThrow(() -> new RuntimeException("단어 없음"));

        savedTermRepository.findByUserAndTerm(user, term)
                .ifPresent(savedTermRepository::delete);
    }

    // 7. 나만의 단어장 조회
    public List<UserSavedTermDto> getMySavedTerms(User user) {
        return savedTermRepository.findAllByUser(user).stream().map(s -> {
            UserSavedTermDto dto = new UserSavedTermDto();
            dto.setTermId(s.getTerm().getId());
            dto.setTerm(s.getTerm().getTerm());
            dto.setDefinition(s.getTerm().getDefinition());
            dto.setExampleSentences(s.getTerm().getExampleSentences());
            return dto;
        }).collect(Collectors.toList());
    }

    // 8. 학습률 조회
    public UserLearningStatusDto getUserLearningStatus(User user) {
        long count = userLearningRepository.countByUserAndLearnedTrue(user);
        UserLearningStatusDto dto = new UserLearningStatusDto();
        dto.setTotalLearnedCount((int) count);
        return dto;
    }

    @Transactional
    public void createTerm(TermCreateDto dto) {
        FinancialCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("카테고리 ID가 유효하지 않습니다."));

        FinancialTerm term = FinancialTerm.builder()
                .term(dto.getTerm())
                .definition(dto.getDefinition())
                .exampleSentences(dto.getExampleSentences())
                .category(category)
                .dayIndex(dto.getDayIndex())
                .build();

        termRepository.save(term);
    }


}

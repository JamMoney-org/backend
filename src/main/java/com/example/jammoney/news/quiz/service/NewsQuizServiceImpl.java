package com.example.jammoney.news.quiz.service;

import com.example.jammoney.news.entity.News;
import com.example.jammoney.news.quiz.controller.QuizResultDto;
import com.example.jammoney.news.entity.NewsQuiz;
import com.example.jammoney.news.quiz.controller.QuizSubmissionDto;
import com.example.jammoney.news.repository.NewsQuizRepository;
import com.example.jammoney.news.repository.NewsRepository;
import com.example.jammoney.news.quiz.gpt.GptNewsQuizData;
import com.example.jammoney.news.quiz.gpt.GptNewsQuizPromptBuilder;
import com.example.jammoney.news.quiz.gpt.GptNewsQuizResponseParser;
import com.example.jammoney.news.quiz.gpt.NewsGptApiClient;
import com.example.jammoney.news.dto.NewsQuizDto;
import com.example.jammoney.pet.service.PetService;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class NewsQuizServiceImpl implements NewsQuizService {

    private final NewsRepository newsRepo;
    private final NewsQuizRepository quizRepo;

    private final NewsGptApiClient gptClient;
    private final PetService petService;
    private final GptNewsQuizPromptBuilder promptBuilder;
    private final GptNewsQuizResponseParser parser;

    @Transactional
    @Override
    public NewsQuizDto getOrCreateQuiz(Long newsId) {
        News news = newsRepo.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("뉴스가 없습니다. id=" + newsId));

        return quizRepo.findByNews(news)
                .map(quiz -> NewsQuizDto.builder()
                        .quizId(quiz.getId())
                        .question(quiz.getQuestion())
                        .option1(quiz.getOption1())
                        .option2(quiz.getOption2())
                        .option3(quiz.getOption3())
                        .option4(quiz.getOption4())
                        .correctAnswerIndex(null)
                        .build())
                .orElseGet(() -> generateAndSaveQuiz(newsId));  // 없으면 생성
    }


    @Transactional
    @Override
    public NewsQuizDto generateAndSaveQuiz(Long newsId) {
        News news = newsRepo.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("뉴스가 없습니다. id=" + newsId));

        String prompt = promptBuilder.buildQuizPrompt(news.getContent());
        GptNewsQuizData data = gptClient
                .callGptRaw(prompt)
                .map(parser::parse)
                .block();

        NewsQuiz quiz = quizRepo.findByNews(news)
                .map(existing -> {
                    existing.setQuestion(data.question());
                    existing.setOption1(data.options().get(0));
                    existing.setOption2(data.options().get(1));
                    existing.setOption3(data.options().get(2));
                    existing.setOption4(data.options().get(3));
                    existing.setCorrectAnswerIndex(data.answerIndex());
                    return existing;
                })
                .orElseGet(() -> {
                    return NewsQuiz.builder()
                            .news(news)
                            .question(data.question())
                            .option1(data.options().get(0))
                            .option2(data.options().get(1))
                            .option3(data.options().get(2))
                            .option4(data.options().get(3))
                            .correctAnswerIndex(data.answerIndex())
                            .build();
                });

        NewsQuiz saved = quizRepo.save(quiz);

        return NewsQuizDto.builder()
                .quizId(saved.getId())
                .question(saved.getQuestion())
                .option1(saved.getOption1())
                .option2(saved.getOption2())
                .option3(saved.getOption3())
                .option4(saved.getOption4())
                .correctAnswerIndex(null)
                .build();
    }

    @Override
    public boolean checkAnswer(Long quizId, int selectedOptionIndex) {
        NewsQuiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("퀴즈가 없습니다. id=" + quizId));
        return quiz.getCorrectAnswerIndex().equals(selectedOptionIndex);
    }

    @Override
    public NewsQuiz findQuizById(Long quizId) {
        return quizRepo.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("퀴즈를 찾을 수 없습니다. id=" + quizId));
    }

    @Override
    @Transactional
    public QuizResultDto submitQuiz(Long newsId, QuizSubmissionDto submission, User user) {
        NewsQuiz quiz = quizRepo.findById(submission.getQuizId())
                .orElseThrow(() -> new IllegalArgumentException("퀴즈가 없습니다. id=" + submission.getQuizId()));

        boolean correct = quiz.getCorrectAnswerIndex().equals(submission.getSelectedIndex());
        petService.addExp(user, 10);
        return new QuizResultDto(correct, quiz.getCorrectAnswerIndex());
    }


}

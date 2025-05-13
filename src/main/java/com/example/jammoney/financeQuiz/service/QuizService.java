package com.example.jammoney.financeQuiz.service;

import com.example.jammoney.financeQuiz.dto.*;

import java.util.List;

public interface QuizService {
    List<QuizQuestionDTO> generateQuizQuestions(QuizStartRequestDTO requestDTO);

    QuizAnswerResultResponseDTO checkAnswer(QuizAnswerSubmitRequestDTO submitDTO);

    void saveWrongNote(WrongNoteSaveRequestDTO requestDTO);

    List<WrongNoteResponseDTO> getWrongNotes(Long userId);
}

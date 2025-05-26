package com.example.jammoney.financeQuiz.service;

import com.example.jammoney.financeQuiz.dto.WrongNoteRequest;
import com.example.jammoney.financeQuiz.dto.WrongNoteResponse;
import com.example.jammoney.user.entity.User;

import java.util.List;

public interface WrongNoteService {
    void saveWrongNote(WrongNoteRequest request, User user);
    List<WrongNoteResponse> getWrongNotesByUser(User user);
    void deleteWrongNote(Long id, User user);
    WrongNoteResponse getWrongNoteById(Long id, User user);
}
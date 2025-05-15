package com.example.jammoney.financeQuiz.service;


import com.example.jammoney.financeQuiz.dto.WrongNoteRequest;
import com.example.jammoney.financeQuiz.dto.WrongNoteResponse;
import com.example.jammoney.financeQuiz.entity.WrongNote;
import com.example.jammoney.financeQuiz.repository.WrongNoteRepository;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WrongNoteServiceImpl implements WrongNoteService {

    private final WrongNoteRepository wrongNoteRepository;

    @Override
    public void saveWrongNote(WrongNoteRequest request, User user) {
        WrongNote wrongNote = WrongNote.builder()
                .user(user)
                .question(request.getQuestion())
                .selectedOption(request.getSelectedOption())
                .correctAnswer(request.getCorrectAnswer())
                .explanation(request.getExplanation())
                .hint(request.getHint())
                .category(request.getCategory())
                .build();

        wrongNoteRepository.save(wrongNote);
    }

    @Override
    public List<WrongNoteResponse> getWrongNotesByUser(User user) {
        return wrongNoteRepository.findAllByUser(user).stream()
                .map(note -> WrongNoteResponse.builder()
                        .id(note.getId())
                        .question(note.getQuestion())
                        .selectedOption(note.getSelectedOption())
                        .correctAnswer(note.getCorrectAnswer())
                        .explanation(note.getExplanation())
                        .hint(note.getHint())
                        .category(note.getCategory())
                        .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    public void deleteWrongNote(Long id, User user) {
        WrongNote note = wrongNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 오답노트를 찾을 수 없습니다."));

        // 본인의 오답노트인지 확인
        if (!note.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인의 오답노트만 삭제할 수 있습니다.");
        }

        wrongNoteRepository.delete(note);
    }
}
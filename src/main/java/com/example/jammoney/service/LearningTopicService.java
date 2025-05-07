package com.example.jammoney.service;

import com.example.jammoney.repository.LearningTopicRepository;
import org.springframework.stereotype.Service;

@Service
public class LearningTopicService {
    private final LearningTopicRepository learningTopicRepository;

    public LearningTopicService(LearningTopicRepository learningTopicRepository) {
        this.learningTopicRepository = learningTopicRepository;
    }
}

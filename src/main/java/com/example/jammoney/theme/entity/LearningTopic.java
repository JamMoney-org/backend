<<<<<<<< HEAD:src/main/java/com/example/jammoney/ThemeLearning/models/LearningTopic.java
package com.example.jammoney.ThemeLearning.models;
========
package com.example.jammoney.theme.entity;
>>>>>>>> develop:src/main/java/com/example/jammoney/theme/entity/LearningTopic.java

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "learning_topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;
}

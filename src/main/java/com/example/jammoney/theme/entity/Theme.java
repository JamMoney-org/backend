<<<<<<<< HEAD:src/main/java/com/example/jammoney/ThemeLearning/models/Theme.java
package com.example.jammoney.ThemeLearning.models;
========
package com.example.jammoney.theme.entity;
>>>>>>>> develop:src/main/java/com/example/jammoney/theme/entity/Theme.java

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "themes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
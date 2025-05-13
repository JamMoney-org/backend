<<<<<<<< HEAD:src/main/java/com/example/jammoney/financeTerm/models/UserTermLearning.java
package com.example.jammoney.financeTerm.models;
========
package com.example.jammoney.financeTerm.entity;
>>>>>>>> develop:src/main/java/com/example/jammoney/financeTerm/entity/UserTermLearning.java

import com.example.jammoney.user.entity.User;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "user_term_learnings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "term_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTermLearning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

<<<<<<<< HEAD:src/main/java/com/example/jammoney/financeTerm/models/UserTermLearning.java
    /*
========

>>>>>>>> develop:src/main/java/com/example/jammoney/financeTerm/entity/UserTermLearning.java
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
     */


    @ManyToOne
    @JoinColumn(name = "term_id")
    private FinancialTerm term;

    private boolean learned;

    private boolean expGiven;
}

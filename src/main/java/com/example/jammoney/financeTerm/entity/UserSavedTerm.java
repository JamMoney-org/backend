<<<<<<<< HEAD:src/main/java/com/example/jammoney/financeTerm/models/UserSavedTerm.java
package com.example.jammoney.financeTerm.models;
========
package com.example.jammoney.financeTerm.entity;
>>>>>>>> develop:src/main/java/com/example/jammoney/financeTerm/entity/UserSavedTerm.java

import com.example.jammoney.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_saved_terms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSavedTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

<<<<<<<< HEAD:src/main/java/com/example/jammoney/financeTerm/models/UserSavedTerm.java
    /*
========

>>>>>>>> develop:src/main/java/com/example/jammoney/financeTerm/entity/UserSavedTerm.java
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    */


    @ManyToOne
    @JoinColumn(name = "term_id")
    private FinancialTerm term;
}


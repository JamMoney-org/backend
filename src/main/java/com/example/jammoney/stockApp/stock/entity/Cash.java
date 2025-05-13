package com.example.jammoney.stockApp.stock.entity;
import com.example.jammoney.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Cash{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long cashId;

    @Column(nullable = false)
    private long money;

    @JoinColumn(name = "MEMBER_ID")
    @OneToOne(fetch = FetchType.LAZY)
    private User user;

}

package ru.yappy.docstorage.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "verification_tokens")
public class VerificationToken {
    @Id
    @Column(name = "token", nullable = false, unique = true)
    private String token;
    @OneToOne
    @JoinColumn(nullable = false, name = "user_id")
    private User user;
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

}
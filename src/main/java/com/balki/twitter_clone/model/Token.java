package com.balki.twitter_clone.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tokens")
public class Token {

    @Id
    private String accessToken;

    private String refreshToken;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}

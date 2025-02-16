package com.balki.twitter_clone.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "twits")
@AllArgsConstructor
@NoArgsConstructor
public class Twitter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(length = 1000)
    private String content;

    private LocalDateTime timestamp;

    @ManyToOne
    private User user;

    @OneToOne(mappedBy = "twitter", orphanRemoval = true)
    private FileAttachment fileAttachment;
}

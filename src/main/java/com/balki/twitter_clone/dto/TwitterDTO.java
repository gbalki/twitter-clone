package com.balki.twitter_clone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwitterDTO {

    private long id;

    private String content;

    private LocalDateTime timestamp;

    private UserDTO user;

    private FileAttachmentDTO fileAttachment;
}

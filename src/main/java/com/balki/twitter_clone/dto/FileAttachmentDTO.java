package com.balki.twitter_clone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileAttachmentDTO {

    private long id;

    private String name;

    private String fileType;

    private LocalDateTime date;
}

package com.balki.twitter_clone.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwitterSaveRequest {

    @NotBlank(message = "not blank")
    @Size(min = 1, max = 1000)
    private String content;

    private long attachmentId;
}

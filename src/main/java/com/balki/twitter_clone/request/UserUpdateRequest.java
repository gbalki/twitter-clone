package com.balki.twitter_clone.request;

import com.balki.twitter_clone.annotation.FileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

    @NotBlank(message = "{twitter.clone.constraint.firstname.notblank}")
    @Size(min = 2, max = 255)
    private String firstName;

    @NotBlank(message = "{twitter.clone.constraint.lastname.notblank}")
    @Size(min = 2, max = 255)
    private String lastName;

    @NotBlank(message = "{twitter.clone.constraint.displayname.notblank}")
    @Size(min = 4, max = 255)
    private String displayName;

    @FileType(types = {"jpeg","png"})
    private String image;
}

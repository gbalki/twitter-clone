package com.balki.twitter_clone.request;

import com.balki.twitter_clone.validation.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSaveRequest {

    @NotBlank(message = "{twitter.clone.constraint.firstname.notblank}")
    @Size(min = 2, max = 255)
    private String firstName;

    @NotBlank(message = "{twitter.clone.constraint.lastname.notblank}")
    @Size(min = 2, max = 255)
    private String lastName;

    @NotBlank(message = "{twitter.clone.constraint.displayname.notblank}")
    @Size(min = 4, max = 255)
    private String displayName;

    @Email(message = "{twitter.clone.constraint.email.format}")
    @NotBlank(message = "{twitter.clone.constraint.email.notblank}")
    @UniqueEmail
    private String email;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "{twitter.clone.constraint.password.pattern.message}")
    private String password;
}

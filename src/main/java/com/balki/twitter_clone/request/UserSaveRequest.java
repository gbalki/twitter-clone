package com.balki.twitter_clone.request;

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

    @NotBlank(message = "first name can not be blank")
    @Size(min = 2, max = 255)
    private String firstName;

    @NotBlank(message = "last name can not be blank")
    @Size(min = 2, max = 255)
    private String lastName;

    @NotBlank(message = "display name can not be blank")
    @Size(min = 4, max = 255)
    private String displayName;

    @Email(message = "should be email format")
    @NotBlank(message = "email can not be blank")
    private String email;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "Password must have at least 1 uppercase, 1 lowercase letter and 1 number")
    private String password;
}

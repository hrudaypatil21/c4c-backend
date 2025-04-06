package com.tisd.c4change.DTO;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public abstract class BaseUserDto {
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    protected String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    protected String password;
}
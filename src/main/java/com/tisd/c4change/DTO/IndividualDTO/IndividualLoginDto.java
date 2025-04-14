package com.tisd.c4change.DTO.IndividualDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndividualLoginDto {
    @NotBlank
    @Email
    private String email;

    @NotBlank @Size(min=8)
    private String password;
}

package com.tisd.c4change.DTO.NgoDTO;

import lombok.*;
import javax.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
public class NGOLoginDto {
    @NotBlank @Email
    private String email;

    @NotBlank @Size(min=8)
    private String password;
}

package com.tisd.c4change.DTO.NgoDTO;

import lombok.*;
import javax.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
public class NGOLoginDto {
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}

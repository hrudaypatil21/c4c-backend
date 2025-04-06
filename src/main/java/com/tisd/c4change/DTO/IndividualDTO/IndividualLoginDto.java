package com.tisd.c4change.DTO.IndividualDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// For login
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndividualLoginDto {
    private String email;
    private String password;
}

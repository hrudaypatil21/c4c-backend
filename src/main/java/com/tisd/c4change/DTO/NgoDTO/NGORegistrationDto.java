package com.tisd.c4change.DTO.NgoDTO;

import com.tisd.c4change.DTO.BaseUserDto;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.util.List;

// For creating a new NGO profile
@Getter
@Setter
@NoArgsConstructor
public class NGORegistrationDto {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String orgName;

    @NotBlank
    private String regNumber;

    @NotBlank
    @Size(min = 8)
    private String password;

    private String phone;
    private String address;
    private String mission;
    private String website;

    @NotEmpty
    private List<String> volNeeds;

    @NotNull
    private MultipartFile verificationDocs;
}


package com.tisd.c4change.DTO.IndividualDTO;

import com.tisd.c4change.DTO.BaseUserDto;
import com.tisd.c4change.Entity.Availability;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.util.List;

// For creating a new Individual User
@Getter
@Setter
@NoArgsConstructor
public class IndividualRegistrationDto {
    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    private String location;
    private String phone;
    private String address;
    private String bio;

    @NotEmpty
    private List<String> skills;

    @NotEmpty
    private List<String> interests;

    @NotNull
    private Availability availability;

    @NotNull
    private MultipartFile resume;


}
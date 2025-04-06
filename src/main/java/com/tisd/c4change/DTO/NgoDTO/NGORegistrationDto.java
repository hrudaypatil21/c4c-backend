package com.tisd.c4change.DTO.NgoDTO;

import com.tisd.c4change.DTO.BaseUserDto;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.util.List;

// For creating a new NGO profile
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
public class NGORegistrationDto extends BaseUserDto {
    @NotBlank
    private String orgName;

    @NotBlank
    private String regNumber;

    private String orgPhone;
    private String orgAddress;
    private String orgMission;
    private String orgWebsite;
    private List<String> volNeeds;
    private MultipartFile verificationDocs;
}


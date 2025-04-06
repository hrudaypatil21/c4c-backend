package com.tisd.c4change.DTO.NgoDTO;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

// For returning NGO data to frontend (no sensitive info)
@Getter
@Setter
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class NGOResponseDto {
    private Long id;
    private String email;
    private String orgName;
    private String regNumber;
    private String orgPhone;
    private String orgAddress;
    private String orgMission;
    private String orgWebsite;
    private List<String> volNeeds;
    private boolean hasVerificationDocs;
}

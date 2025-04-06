package com.tisd.c4change.DTO.IndividualDTO;

import com.tisd.c4change.Entity.Availability;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

// For returning Individual User data to frontend (no sensitive info)
@Getter
@Setter
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class IndividualResponseDto {
    private Long id;
    private String email;
    private String name;
    private String location;
    private String phone;
    private String address;
    private String bio;
    private List<String> skills;
    private List<String> interests;
    private Availability availability;
    private boolean hasResume;


}

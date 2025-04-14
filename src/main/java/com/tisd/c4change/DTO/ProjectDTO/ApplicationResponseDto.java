package com.tisd.c4change.DTO.ProjectDTO;

import com.tisd.c4change.Entity.ApplicationStatus;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class ApplicationResponseDto {
    private Long id;
    private Long projectId;
    private String projectTitle;
    private Long volunteerId;
    private String volunteerName;
    private ApplicationStatus status;
//    private String message;
    private String appliedAt;
}

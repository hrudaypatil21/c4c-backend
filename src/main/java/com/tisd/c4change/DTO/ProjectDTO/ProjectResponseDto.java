package com.tisd.c4change.DTO.ProjectDTO;

import com.tisd.c4change.Entity.ProjectStatus;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class ProjectResponseDto {
    private Long id;
    private String title;
    private String description;
    private String location;
    private List<String> skills;
    private String status;
    private String ngoName;
}
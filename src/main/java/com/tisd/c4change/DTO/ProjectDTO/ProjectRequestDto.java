package com.tisd.c4change.DTO.ProjectDTO;

import com.tisd.c4change.Entity.ProjectStatus;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ProjectRequestDto {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private ProjectStatus status;

    @NotNull
    @FutureOrPresent
    private LocalDateTime startedAt;

    @Future
    private LocalDateTime endedAt;

    @NotBlank
    private String location;

    private List<String> skills;

    @NotNull
    private Long ngoId;
}

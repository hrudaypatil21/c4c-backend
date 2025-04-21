package com.tisd.c4change.DTO.ProjectDTO;

import com.tisd.c4change.Entity.Project;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectMatch {
    private Project project;
    private double similarityScore;
}
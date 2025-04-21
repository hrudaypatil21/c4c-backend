package com.tisd.c4change.DTO.ProjectDTO;

import com.tisd.c4change.Entity.Project;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectMatchDTO {
    private ProjectResponseDto project;
    private double similarityScore;

    public ProjectMatchDTO(Project project, double similarityScore) {
        this.project = convertToDto(project);
        this.similarityScore = similarityScore;
    }

    private ProjectResponseDto convertToDto(Project project) {
        return new ProjectResponseDto()
                .setId(project.getId())
                .setTitle(project.getTitle())
                .setDescription(project.getDescription())
                .setLocation(project.getLocation())
                .setSkills(project.getSkills())
                .setStatus(project.getStatus().toString())
                .setNgoName(project.getNgo().getOrgName());
    }
}
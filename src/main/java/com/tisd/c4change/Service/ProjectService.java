package com.tisd.c4change.Service;

import com.tisd.c4change.CustomException.ConflictException;
import com.tisd.c4change.CustomException.ResourceNotFoundException;
import com.tisd.c4change.CustomException.UserNotFoundException;
import com.tisd.c4change.DTO.ProjectDTO.*;
import com.tisd.c4change.Entity.*;
import com.tisd.c4change.Repository.IndividualRepository;
import com.tisd.c4change.Repository.NGORepository;
import com.tisd.c4change.Repository.ProjectApplicationRepository;
import com.tisd.c4change.Repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProjectService {

    @Autowired
    private final ProjectRepository projectRepository;
    @Autowired
    private final NGORepository ngoRepository;

    private final ModelMapper modelMapper;
//    private final SkillMatchingService skillMatchingService;
    @Autowired
    private final IndividualRepository individualRepository;


//    // Get volunteer skills
//    IndividualUser volunteer = individualRepository.findById(volunteerId)
//            .orElseThrow(() -> new ResourceNotFoundException("Volunteer not found"));
//
//    // Get all projects
//    List<Project> allProjects = projectRepository.findAll();
    SkillMatchingService skillMatchingService;
//
    public List<ProjectMatch> getRecommendedProjects(Long volunteerId) {
        // Get volunteer skills
        IndividualUser volunteer = individualRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer not found"));

        // Get all projects
        List<Project> allProjects = projectRepository.findAll();

        // Match skills
        return skillMatchingService.matchVolunteerToProjects(
                volunteer.getSkills(),
                allProjects
        );
    }



    public ProjectResponseDto createProject(ProjectRequestDto projectDto) {
        // 1. Validate NGO exists
        NGOProfile ngo = ngoRepository.findById(projectDto.getNgoId())
                .orElseThrow(() -> new ResourceNotFoundException("NGO not found with id: " + projectDto.getNgoId()));

        // 2. Create project entity
        Project project = new Project();
        project.setTitle(projectDto.getTitle());
        project.setDescription(projectDto.getDescription());
        project.setStatus(projectDto.getStatus());
        project.setStartedAt(projectDto.getStartedAt());
        project.setEndedAt(projectDto.getEndedAt());
        project.setLocation(projectDto.getLocation());
        project.setSkills(projectDto.getSkills() != null ? projectDto.getSkills() : new ArrayList<>());
        project.setNgo(ngo);

        // 3. Save project
        Project savedProject = projectRepository.save(project);
        return convertToDto(savedProject);
    }


    public List<ProjectResponseDto> getProjectsByNgo(Long ngoId) {
        return projectRepository.findByNgoId(ngoId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ProjectResponseDto convertToDto(Project project) {
        ProjectResponseDto dto = modelMapper.map(project, ProjectResponseDto.class);
        dto.setNgoName(project.getNgo().getOrgName());
        return dto;
    }
}
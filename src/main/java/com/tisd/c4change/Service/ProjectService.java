package com.tisd.c4change.Service;

import com.tisd.c4change.CustomException.ConflictException;
import com.tisd.c4change.CustomException.ResourceNotFoundException;
import com.tisd.c4change.CustomException.UserNotFoundException;
import com.tisd.c4change.DTO.ProjectDTO.ApplicationDto;
import com.tisd.c4change.DTO.ProjectDTO.ApplicationResponseDto;
import com.tisd.c4change.Entity.ApplicationStatus;
import com.tisd.c4change.Entity.IndividualUser;
import com.tisd.c4change.Entity.Project;
import com.tisd.c4change.Entity.ProjectApplication;
import com.tisd.c4change.Repository.IndividualRepository;
import com.tisd.c4change.Repository.NGORepository;
import com.tisd.c4change.Repository.ProjectApplicationRepository;
import com.tisd.c4change.Repository.ProjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final IndividualRepository individualRepository;
    private final NGORepository ngoRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, ProjectApplicationRepository projectApplicationRepository, IndividualRepository individualRepository, NGORepository ngoRepository) {
        this.projectRepository = projectRepository;
        this.projectApplicationRepository = projectApplicationRepository;
        this.individualRepository = individualRepository;
        this.ngoRepository = ngoRepository;
    }

    @Transactional
    public ApplicationResponseDto applyForProject(ApplicationDto applicationDto) {
        IndividualUser individualUser = individualRepository.findIndUserById(applicationDto.getVolunteerId())
                .orElseThrow(() -> new UserNotFoundException("Volunteer not found"));

        Project project = projectRepository.findById(applicationDto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (projectApplicationRepository.existsByProjectIdAndVolunteerId(applicationDto.getVolunteerId(), applicationDto.getProjectId())) {
            throw new ConflictException("Already applied");
        }

        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setVolunteer(individualUser);
        projectApplication.setProject(project);

        ProjectApplication savedApplication = projectApplicationRepository.save(projectApplication);

        return convertToDto(savedApplication);
    }

    private ApplicationResponseDto saveApplication(Long applicationId, ApplicationStatus status, Long ngoId) {
        ProjectApplication application = projectApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getProject().getNgo().getId().equals(ngoId)) {
            throw new ConflictException("You don't have permission to process this application");
        }

        application.setStatus(status);
        ProjectApplication updatedApplication = projectApplicationRepository.save(application);

        return convertToDto(updatedApplication);
    }

    private ApplicationResponseDto convertToDto(ProjectApplication application) {
        ApplicationResponseDto dto = new ApplicationResponseDto();
        dto.setId(application.getId());
        dto.setProjectId(dto.getProjectId());
        dto.setProjectTitle(dto.getProjectTitle());
        dto.setVolunteerId(dto.getVolunteerId());
        dto.setVolunteerName(dto.getVolunteerName());
        dto.setStatus(dto.getStatus());
        dto.setAppliedAt(dto.getAppliedAt());

        return dto;
    }
}

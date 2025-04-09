package com.tisd.c4change.Controller;

import com.tisd.c4change.CustomException.ConflictException;
import com.tisd.c4change.CustomException.ResourceNotFoundException;
import com.tisd.c4change.DTO.ProjectDTO.ApplicationDto;
import com.tisd.c4change.DTO.ProjectDTO.ApplicationResponseDto;
import com.tisd.c4change.Entity.ApplicationStatus;
import com.tisd.c4change.Entity.IndividualUser;
import com.tisd.c4change.Entity.Project;
import com.tisd.c4change.Entity.ProjectApplication;
import com.tisd.c4change.Repository.IndividualRepository;
import com.tisd.c4change.Repository.ProjectApplicationRepository;
import com.tisd.c4change.Repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ProjectApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final IndividualRepository individualRepository;
    private final ModelMapper modelMapper;

    // Volunteer applies for a project
    @PostMapping
    public ResponseEntity<ApplicationResponseDto> applyForProject(
            @RequestBody @Valid ApplicationDto applicationDto,
            @RequestHeader("Authorization") String token) {

        Project project = projectRepository.findById(applicationDto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        IndividualUser volunteer = individualRepository.findById(applicationDto.getVolunteerId())
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer not found"));

        // Check if application already exists
        if (applicationRepository.existsByProjectIdAndVolunteerId(
                applicationDto.getProjectId(), applicationDto.getVolunteerId())) {
            throw new ConflictException("You have already applied for this project");
        }

        ProjectApplication application = new ProjectApplication();
        application.setProject(project);
        application.setVolunteer(volunteer);
        application.setStatus(ApplicationStatus.PENDING);

        ProjectApplication savedApplication = applicationRepository.save(application);
        return ResponseEntity.ok(convertToDto(savedApplication));
    }

    // NGO views applications for their projects
    @GetMapping("/ngo/{ngoId}")
    public ResponseEntity<List<ApplicationResponseDto>> getApplicationsForNgo(
            @PathVariable Long ngoId,
            @RequestHeader("Authorization") String token) {

        List<ProjectApplication> applications = applicationRepository.findByProjectNgoId(ngoId);
        return ResponseEntity.ok(applications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));
    }

    // Volunteer views their applications
    @GetMapping("/volunteer/{volunteerId}")
    public ResponseEntity<List<ApplicationResponseDto>> getApplicationsForVolunteer(
            @PathVariable Long volunteerId,
            @RequestHeader("Authorization") String token) {

        List<ProjectApplication> applications = applicationRepository.findByVolunteerId(volunteerId);
        return ResponseEntity.ok(applications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));
    }

    // NGO updates application status
    @PutMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponseDto> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestParam ApplicationStatus status,
            @RequestHeader("Authorization") String token) {

        ProjectApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        application.setStatus(status);
        ProjectApplication updatedApplication = applicationRepository.save(application);
        return ResponseEntity.ok(convertToDto(updatedApplication));
    }

    private ApplicationResponseDto convertToDto(ProjectApplication application) {
        ApplicationResponseDto dto = modelMapper.map(application, ApplicationResponseDto.class);
        dto.setProjectTitle(application.getProject().getTitle());
        dto.setVolunteerName(application.getVolunteer().getName());
        dto.setAppliedAt(application.getAppliedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return dto;
    }
}
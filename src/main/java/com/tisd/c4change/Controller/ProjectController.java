package com.tisd.c4change.Controller;

import com.google.firebase.auth.FirebaseAuth;
import com.tisd.c4change.CustomException.ResourceNotFoundException;
import com.tisd.c4change.DTO.ProjectDTO.*;
import com.tisd.c4change.Entity.NGOProfile;
import com.tisd.c4change.Entity.Project;
import com.tisd.c4change.Mapper.DtoConverter;
import com.tisd.c4change.Repository.NGORepository;
import com.tisd.c4change.Repository.ProjectRepository;
import com.tisd.c4change.Service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/projects")
@RequiredArgsConstructor
public class ProjectController {

    @Autowired
    private final ProjectService projectService;
//    private final FirebaseAuth firebaseAuth;
    @Autowired
    private NGORepository ngoRepository;
    @Autowired
    private ProjectRepository projectRepository;

    @PostMapping
    public ResponseEntity<?> createProject(
            @RequestBody @Valid ProjectRequestDto projectDto,
            Authentication authentication) {

        try {
            // Get authenticated user details
            @SuppressWarnings("unchecked")
            Map<String, String> principal = (Map<String, String>) authentication.getPrincipal();
            String firebaseUid = principal.get("uid");

            // Find NGO by Firebase UID
            NGOProfile ngo = ngoRepository.findByFirebaseUid(firebaseUid)
                    .orElseThrow(() -> new ResourceNotFoundException("NGO not found"));

            // Create enriched DTO with NGO ID
            ProjectRequestDto enrichedDto = new ProjectRequestDto()
                    .setTitle(projectDto.getTitle())
                    .setDescription(projectDto.getDescription())
                    .setStatus(projectDto.getStatus())
                    .setStartedAt(projectDto.getStartedAt())
                    .setEndedAt(projectDto.getEndedAt())
                    .setLocation(projectDto.getLocation())
                    .setSkills(projectDto.getSkills())
                    .setNgoId(ngo.getId());

            ProjectResponseDto createdProject = projectService.createProject(enrichedDto);
            return ResponseEntity.ok(createdProject);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Project creation failed: " + e.getMessage()));
        }
    }

    @GetMapping("/ngo/{ngoId}")
    public ResponseEntity<List<ProjectResponseDto>> getProjectsByNgo(
            @PathVariable Long ngoId,
            @RequestHeader("Authorization") String token) {
        List<ProjectResponseDto> projects = projectService.getProjectsByNgo(ngoId);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/recommended")
    public ResponseEntity<List<ProjectMatch>> getRecommendedProjects(
            @RequestParam Long volunteerId,
            @RequestHeader("Authorization") String token) {

        // Verify the volunteerId matches the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        List<ProjectMatch> recommendedProjects = projectService.getRecommendedProjects(volunteerId);
        return ResponseEntity.ok(recommendedProjects);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> getAllProjects() {
        try {
            List<Project> projects = projectRepository.findAll();

            List<ProjectResponseDto> projectDtos = projects.stream()
                    .map(project -> {
                        ProjectResponseDto dto = new ProjectResponseDto();
                        // Map all necessary fields
                        dto.setId(project.getId());
                        dto.setTitle(project.getTitle());
                        dto.setDescription(project.getDescription());
                        dto.setLocation(project.getLocation());
                        dto.setSkills(project.getSkills());
                        dto.setStatus(String.valueOf(project.getStatus()));
                        dto.setNgoName(project.getNgo().getOrgName());
                        // Add other fields as needed
                        return dto;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(projectDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/public")
    public ResponseEntity<List<ProjectResponseDto>> getPublicProjects() {
        List<Project> projects = projectRepository.findAll(); // Or add filter for public projects
        List<ProjectResponseDto> projectDtos = projects.stream()
                .map(DtoConverter::toProjectResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(projectDtos);
    }


    }



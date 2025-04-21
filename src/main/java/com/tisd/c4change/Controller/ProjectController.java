package com.tisd.c4change.Controller;

import com.google.firebase.auth.FirebaseAuth;
import com.tisd.c4change.CustomException.ResourceNotFoundException;
import com.tisd.c4change.DTO.ProjectDTO.*;
import com.tisd.c4change.Entity.NGOProfile;
import com.tisd.c4change.Entity.Project;
import com.tisd.c4change.FirebaseAuthenticationToken;
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
            String firebaseUid;

            if (authentication.getPrincipal() instanceof FirebaseAuthenticationToken) {
                // Handle Firebase token case
                FirebaseAuthenticationToken token = (FirebaseAuthenticationToken) authentication.getPrincipal();
                firebaseUid = token.getToken().getUid();
            } else if (authentication.getPrincipal() instanceof String) {
                // Handle string UID case
                firebaseUid = (String) authentication.getPrincipal();
            } else {
                throw new AccessDeniedException("Unsupported principal type");
            }

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
    public ResponseEntity<List<ProjectMatchDTO>> getRecommendedProjects(
            @RequestParam String volunteerId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Debug logging - add this temporarily
        System.out.println("Authentication object: " + authentication);
        if (authentication != null) {
            System.out.println("Principal class: " + authentication.getPrincipal().getClass());
            System.out.println("Principal: " + authentication.getPrincipal());
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        String authenticatedUid;
        if (authentication.getPrincipal() instanceof FirebaseAuthenticationToken) {
            // Handle custom token case
            FirebaseAuthenticationToken token = (FirebaseAuthenticationToken) authentication.getPrincipal();
            authenticatedUid = token.getToken().getUid();
        } else if (authentication.getPrincipal() instanceof String) {
            // Handle string UID case
            authenticatedUid = (String) authentication.getPrincipal();
        } else {
            throw new AccessDeniedException("Unsupported principal type");
        }

        if (!authenticatedUid.equals(volunteerId)) {
            throw new AccessDeniedException("User not authorized to access these recommendations");
        }

        List<ProjectMatch> matches = projectService.getRecommendedProjects(volunteerId);
        List<ProjectMatchDTO> dtos = matches.stream()
                .map(m -> new ProjectMatchDTO(m.getProject(), m.getSimilarityScore()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
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



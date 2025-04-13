package com.tisd.c4change.Controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.tisd.c4change.CustomException.ResourceNotFoundException;
import com.tisd.c4change.DTO.ProjectDTO.*;
import com.tisd.c4change.Entity.NGOProfile;
import com.tisd.c4change.Repository.NGORepository;
import com.tisd.c4change.Service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;


@RequestMapping("api/projects")
    public class ProjectController {

    private final ProjectService projectService;
    private final FirebaseAuth firebaseAuth;
    private NGORepository ngoRepository;

    public ProjectController(ProjectService projectService, FirebaseAuth firebaseAuth) {
        this.projectService = projectService;
        this.firebaseAuth = firebaseAuth;
    }
    @PostMapping
    public ResponseEntity<?> createProject(
            @RequestBody @Valid ProjectRequestDto projectDto,
            Authentication authentication) {

        try {
            // 1. Get NGO profile from Firebase UID
            String firebaseUid = (String) authentication.getPrincipal();
            NGOProfile ngo = ngoRepository.findByFirebaseUid(firebaseUid)
                    .orElseThrow(() -> new ResourceNotFoundException("NGO not found"));

            // 2. Verify the requesting NGO matches the project owner
            if (!ngo.getId().equals(projectDto.getNgoId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only create projects for your own NGO"));
            }

            // 3. Create project
            ProjectResponseDto createdProject = projectService.createProject(projectDto);
            return ResponseEntity.ok(createdProject);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Project creation failed"));
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
    }

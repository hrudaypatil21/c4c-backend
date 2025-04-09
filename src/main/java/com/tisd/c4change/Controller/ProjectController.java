package com.tisd.c4change.Controller;

import com.tisd.c4change.CustomException.ResourceNotFoundException;
import com.tisd.c4change.DTO.ProjectDTO.*;
import com.tisd.c4change.Entity.IndividualUser;
import com.tisd.c4change.Entity.Project;
import com.tisd.c4change.Repository.IndividualRepository;
import com.tisd.c4change.Repository.ProjectRepository;
import com.tisd.c4change.Service.ProjectService;
//import com.tisd.c4change.Service.SkillMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("api/projects")
    public class ProjectController {

    @Autowired
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
            this.projectService = projectService;
        }

    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(
            @RequestBody @Valid ProjectRequestDto projectDto,
            @RequestHeader("Authorization") String token) {
        ProjectResponseDto createdProject = projectService.createProject(projectDto);
        return ResponseEntity.ok(createdProject);
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

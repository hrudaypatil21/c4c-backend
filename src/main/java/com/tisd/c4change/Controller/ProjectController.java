package com.tisd.c4change.Controller;

import com.tisd.c4change.DTO.ProjectDTO.ApplicationDto;
import com.tisd.c4change.DTO.ProjectDTO.ApplicationResponseDto;
import com.tisd.c4change.Service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("api/projects")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/apply")
    public ResponseEntity<ApplicationResponseDto> applyForProject(@RequestBody ApplicationDto applicationDto) {
        ApplicationResponseDto response = projectService.applyForProject(applicationDto);
        return ResponseEntity.ok(response);
    }

    
}

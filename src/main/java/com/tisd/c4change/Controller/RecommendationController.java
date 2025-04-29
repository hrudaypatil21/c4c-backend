package com.tisd.c4change.Controller;

import com.tisd.c4change.DTO.ProjectDTO.ProjectMatch;
import com.tisd.c4change.DTO.ProjectDTO.ProjectMatchDTO;
import com.tisd.c4change.FirebaseAuthenticationToken;
import com.tisd.c4change.Repository.NGORepository;
import com.tisd.c4change.Repository.ProjectRepository;
import com.tisd.c4change.Service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class RecommendationController {

    @Autowired
    private final ProjectService projectService;
    //    private final FirebaseAuth firebaseAuth;
    @Autowired
    private NGORepository ngoRepository;
    @Autowired
    private ProjectRepository projectRepository;

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

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore()) // or other cache control directives
                .body(dtos);
    }
}

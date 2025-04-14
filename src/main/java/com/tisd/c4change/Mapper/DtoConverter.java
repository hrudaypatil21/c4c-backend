package com.tisd.c4change.Mapper;

import com.tisd.c4change.DTO.IndividualDTO.IndividualResponseDto;
import com.tisd.c4change.DTO.NgoDTO.NGOResponseDto;
import com.tisd.c4change.DTO.ProjectDTO.ProjectResponseDto;
import com.tisd.c4change.Entity.IndividualUser;
import com.tisd.c4change.Entity.NGOProfile;
import com.tisd.c4change.Entity.Project;

public class DtoConverter {
    public static IndividualResponseDto toIndividualResponseDto(IndividualUser individualUser) {
        IndividualResponseDto dto = new IndividualResponseDto();
        dto.setId(individualUser.getId());
        dto.setEmail(individualUser.getEmail());
        dto.setName(individualUser.getName());
        dto.setLocation(individualUser.getLocation());
        dto.setPhone(individualUser.getPhone());
        dto.setAddress(individualUser.getAddress());
        dto.setBio(individualUser.getBio());
        dto.setSkills(individualUser.getSkills());
        dto.setInterests(individualUser.getInterests());
        dto.setAvailability(individualUser.getAvailability());
        dto.setHasResume(individualUser.getResumePath() != null && individualUser.getResumePath().length > 0);

        return dto;
    }

    public static NGOResponseDto toNGOResponseDto(NGOProfile ngo) {
        NGOResponseDto dto = new NGOResponseDto();
        dto.setId(ngo.getId());
        dto.setEmail(ngo.getEmail());
        dto.setOrgName(ngo.getOrgName());
        dto.setRegNumber(ngo.getRegNumber());
        dto.setPhone(ngo.getOrgPhone());
        dto.setAddress(ngo.getOrgAddress());
        dto.setMission(ngo.getOrgMission());
        dto.setWebsite(ngo.getOrgWebsite());
        dto.setVolNeeds(ngo.getVolNeeds());
        dto.setHasVerificationDocs(ngo.getVerificationDocsPath() != null && ngo.getVerificationDocsPath().length > 0);
        return dto;
    }

    public static ProjectResponseDto toProjectResponseDto(Project project) {
        ProjectResponseDto dto = new ProjectResponseDto();
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setLocation(project.getLocation());
        dto.setSkills(project.getSkills());
        dto.setStatus(String.valueOf(project.getStatus()));

        return dto;
    }
}

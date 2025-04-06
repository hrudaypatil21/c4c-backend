package com.tisd.c4change.DTO.NgoDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NGOUpdateDto {
    private String orgName;
    private String orgPhone;
    private String orgAddress;
    private String orgMission;
    private String orgWebsite;
    private List<String> volNeeds;

    private MultipartFile verificationDocs;

    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;

    public boolean isPasswordChangeRequested() {
        return newPassword != null && !newPassword.isEmpty();
    }

    public boolean isNewPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmNewPassword);
    }
}
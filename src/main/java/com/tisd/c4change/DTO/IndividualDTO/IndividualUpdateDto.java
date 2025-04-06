package com.tisd.c4change.DTO.IndividualDTO;

import com.tisd.c4change.Entity.Availability;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class IndividualUpdateDto {
    private String name;
    private String location;
    private String phone;
    private String address;
    private String bio;
    private List<String> skills;
    private List<String> interests;
    private Availability availability;

    private MultipartFile resume;

    // Password change fields
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
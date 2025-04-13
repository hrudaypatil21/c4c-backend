package com.tisd.c4change.Entity;

import com.tisd.c4change.Password.PasswordUtil;
import jakarta.persistence.*;
import jdk.jfr.BooleanFlag;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Accessors(chain = true)
@Table(name = "ngo_profiles")
public class NGOProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firebase_uid", unique = true)
    private String firebaseUid;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @Column(nullable = false)
    private String orgName;

    @Column(unique = true, nullable = false)
    private String regNumber;

    private String orgPhone;

    @Column(columnDefinition = "TEXT")
    private String orgAddress;

    @Column(columnDefinition = "TEXT")
    private String orgMission;

    private String orgWebsite;

    @ElementCollection
    @CollectionTable(name = "ngo_volunteer_needs", joinColumns = @JoinColumn(name = "ngo_id"))
    private List<String> volNeeds;

    @Lob
    private byte[] verificationDocsPath;

    private Boolean isVerified;

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        this.email = email;
    }

    public void setPassword(String password) {
        this.passwordHash = PasswordUtil.hashPassword(password);
    }

    public boolean verifyPassword(String password) {
        return PasswordUtil.verifyPassword(password, this.passwordHash);
    }
}

package com.tisd.c4change.Entity;

import com.tisd.c4change.Password.PasswordUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "ngo_users")
public class NGOProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, name = "password")
    private String passwordHash;  // Properly hashed

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

    @Setter
    @Lob
    private byte[] verificationDocsPath;

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
        return password.isEmpty() || PasswordUtil.verifyPassword(password, this.passwordHash);
    }


}

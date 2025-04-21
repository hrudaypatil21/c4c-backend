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
@Entity
@Accessors(chain = true)
@Table(name = "individual_users")
public class IndividualUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firebase_uid", unique = true)
    private String firebaseUid;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, name = "password_hash")  // Match your database column name
    private String passwordHash;

    private String name;
    private String location;
    private String phone;
    private String address;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @ElementCollection
    @CollectionTable(name = "individual_skills", joinColumns = @JoinColumn(name = "user_id"))
    private List<String> skills;

    @ElementCollection
    @CollectionTable(name = "individual_interests", joinColumns = @JoinColumn(name = "user_id"))
    private List<String> interests;

    @Enumerated(EnumType.STRING)
    private Availability availability;

    @Lob
    private byte[] resumePath;

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        this.email = email;
    }

    // Proper password handling
    public void setPassword(String password) {
 // Remove this in production
        this.passwordHash = PasswordUtil.hashPassword(password); // Remove this in production!
    }

    public boolean verifyPassword(String password) {
        return password.isEmpty() || PasswordUtil.verifyPassword(password, this.passwordHash);
    }
}

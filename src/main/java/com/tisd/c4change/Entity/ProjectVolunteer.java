package com.tisd.c4change.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_volunteers")
@Getter
@Setter
public class ProjectVolunteer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "volunteer_id", nullable = false)
    private IndividualUser volunteer;

    @Enumerated(EnumType.STRING)
    private VolunteerStatus status = VolunteerStatus.ACTIVE;

    @CreationTimestamp
    private LocalDateTime joinedAt;

    private Integer hoursContributed = 0;
}
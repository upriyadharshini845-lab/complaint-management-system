package com.cms.complaint.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.cms.complaint.enums.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "escalations")
@Getter @Setter @NoArgsConstructor
public class Escalation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complaint_id")
    private Complaint complaint;

    @Column(nullable = false, length = 255)
    private String reason;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime escalatedAt;
}

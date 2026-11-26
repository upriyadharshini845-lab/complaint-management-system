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
@Table(name = "complaint_attachments")
@Getter @Setter @NoArgsConstructor
public class ComplaintAttachment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complaint_id")
    private Complaint complaint;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Column(length = 100)
    private String contentType;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime uploadedAt;
}

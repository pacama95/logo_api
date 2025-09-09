package com.logo.infrastructure.outgoing.persistence;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "logo")
public class LogoEntity extends PanacheEntityBase {

    @Id
    @SequenceGenerator(name = "logo_seq", sequenceName = "logo_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "logo_seq")
    public Long id;

    @Column(name = "external_identifier", nullable = false, unique = true)
    public String externalIdentifier;

    @Column(name = "resource_url", length = 2048)
    public String resourceUrl;

    @Column(name = "file_content")
    public byte[] fileContent;

    @Column(name = "file_name")
    public String fileName;

    @Column(name = "content_type", length = 100)
    public String contentType;

    @Column(name = "created_at")
    public OffsetDateTime createdAt;

    @Column(name = "updated_at")
    public OffsetDateTime updatedAt;
}

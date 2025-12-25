package com.facture.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /**
     * Tenant ID for multi-tenancy support
     * Each user belongs to one tenant (for future cabinet/team features)
     * Default: user's own ID (self-tenant)
     */
    @Column(name = "tenant_id", nullable = false)
    public Long tenantId;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    public String email;

    @Column(name = "password_hash")
    public String passwordHash;

    @Column(name = "google_id", unique = true)
    public String googleId;

    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false)
    public String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false)
    public String lastName;

    @Column(name = "company_name")
    public String companyName;

    @Size(min = 14, max = 14, message = "SIRET must be 14 digits")
    @Column(length = 14)
    public String siret;

    @Column(name = "address_street")
    public String addressStreet;

    @Column(name = "address_city")
    public String addressCity;

    @Column(name = "address_postal_code")
    public String addressPostalCode;

    @Column(name = "address_country")
    public String addressCountry = "France";

    @Column(name = "phone")
    public String phone;

    @Column(name = "iban")
    public String iban;

    @Column(name = "bic")
    public String bic;

    @Column(name = "otp_secret")
    public String otpSecret;

    @Column(name = "otp_expiry")
    public LocalDateTime otpExpiry;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Client> clients = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Invoice> invoices = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // By default, each user is their own tenant
        if (tenantId == null) {
            tenantId = id;
        }
    }

    @PostPersist
    public void postPersist() {
        // Set tenantId to user's own ID if not already set (for solo users)
        if (tenantId == null) {
            tenantId = id;
            persist();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static User findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public static User findByGoogleId(String googleId) {
        return find("googleId", googleId).firstResult();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isOtpValid() {
        return otpExpiry != null && LocalDateTime.now().isBefore(otpExpiry);
    }
}

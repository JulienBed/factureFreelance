package com.facture.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
public class Client extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @NotBlank(message = "Company name is required")
    @Column(name = "company_name", nullable = false)
    public String companyName;

    @Column(name = "contact_name")
    public String contactName;

    @Email(message = "Email should be valid")
    @Column(nullable = false)
    public String email;

    @Column
    public String phone;

    @Column(name = "address_street")
    public String addressStreet;

    @Column(name = "address_city")
    public String addressCity;

    @Column(name = "address_postal_code")
    public String addressPostalCode;

    @Column(name = "address_country")
    public String addressCountry = "France";

    @Column(length = 14)
    public String siret;

    @Column(name = "tva_number")
    public String tvaNumber;

    @Column(length = 1000)
    public String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Invoice> invoices = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static List<Client> findByUserId(Long userId) {
        return list("user.id", userId);
    }

    public static List<Client> findByUserIdWithSearch(Long userId, String search) {
        return list("user.id = ?1 and (lower(companyName) like ?2 or lower(contactName) like ?2 or lower(email) like ?2)",
                    userId, "%" + search.toLowerCase() + "%");
    }
}

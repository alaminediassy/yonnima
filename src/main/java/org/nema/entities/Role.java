package org.nema.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "roles")
@AllArgsConstructor
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    // Exemple : "ADMIN", "STANDARD_USER", "PROFESSIONAL_DRIVER"
    private String name;

    private String description;

    public Role(String roleName, String description) {
    }
}

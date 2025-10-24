package ru.aston.hometask4.entity;

import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name="users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {
    @Getter @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    @Column(name="name", nullable = false)
    private String name;

    @Getter @Setter
    @Column(name="email", nullable = false, unique = true)
    private String email;

    @Getter @Setter
    @Column(name="age")
    private Integer age;

    @Getter @Setter
    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime createdAt;
}

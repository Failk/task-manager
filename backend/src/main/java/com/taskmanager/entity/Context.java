package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "contexts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "tasks")
public class Context {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 200)
    private String description;

    private String color;

    private String icon;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    @ManyToMany(mappedBy = "contexts")
    @Builder.Default
    private Set<Task> tasks = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // null for system defaults like @home, @office
    
    public static Context createDefault(String name, String icon) {
        return Context.builder()
                .name(name)
                .icon(icon)
                .isDefault(true)
                .build();
    }
}

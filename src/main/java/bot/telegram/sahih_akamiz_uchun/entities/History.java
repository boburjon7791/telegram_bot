package bot.telegram.sahih_akamiz_uchun.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table
@ToString
@Builder
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Builder.Default
    @Column(name = "date_time",nullable = false)
    private LocalDateTime dateTime=LocalDateTime.now();

    @NotBlank
    @Column(nullable = false)
    private String service;

    @NotBlank
    @Column(nullable = false)
    private String phone;
}

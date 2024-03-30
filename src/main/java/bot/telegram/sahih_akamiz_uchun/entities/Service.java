package bot.telegram.sahih_akamiz_uchun.entities;

import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "service")
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @NotNull
    @PositiveOrZero
    @Column(nullable=false)
    private Double price;

    @NotBlank
    @Column(nullable=false)
    private String type;

    @NotBlank
    @Column(nullable = false)
    private String currency;

    @Builder.Default
    private String image="no";

    @Builder.Default
    private String video="no";

    @OneToMany
    private Set<History> historySet;
}

package ca.gb.userservice.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String role; // "student", "staff", or "faculty"

}

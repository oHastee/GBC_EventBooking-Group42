package ca.gbc.roomservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="t_room")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "room_name")
    private String roomName;
    private Integer capacity;
    private String features;

}

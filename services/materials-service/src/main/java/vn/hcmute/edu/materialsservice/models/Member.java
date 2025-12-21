package vn.hcmute.edu.materialsservice.models;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@Table(name = "members")
@SuperBuilder
@NoArgsConstructor
public class Member extends User {

}
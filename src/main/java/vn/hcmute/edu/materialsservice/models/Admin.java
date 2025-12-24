package vn.hcmute.edu.materialsservice.models;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "users")
public class Admin extends User {
    // Add Admin-specific fields here if needed
}
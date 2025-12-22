package vn.hcmute.edu.materialsservice.models;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public abstract class User implements Serializable {
    @Id
    private UUID id;

    @Field("_class")  // MongoDB discriminator field
    private String userType;

    @Field("full_name")
    private String fullName;

    @Field("email")
    private String email;

    @Field("password")
    private String password;

    @Field("is_active")
    private boolean isActive;

    @Field("created_on")
    private LocalDateTime createdOn;

    @Field("modified_on")
    private LocalDateTime modifiedOn;

    // MongoDB doesn't use @PrePersist the same way
    // You'll need to handle this in your service or use MongoTemplate events
}
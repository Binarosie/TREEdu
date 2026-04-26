package vn.hcmute.edu.materialsservice.models;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public abstract class User implements Serializable {
    @Id
    private String id;

    @Field("_class")
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

}
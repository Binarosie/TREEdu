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

    @Field("phone_number") // Đồng bộ trường số điện thoại
    private String phoneNumber;

    @Field("avatar_url") // Đường dẫn ảnh đại diện
    private String avatarUrl;

    @Field("birth_year") // Năm sinh
    private Integer birthYear;

    @Field("address") // Nơi ở
    private String address;

    @Field("gender") // Giới tính (MALE, FEMALE, OTHER)
    private String gender;

    @Field("bio") // Đoạn giới thiệu ngắn
    private String bio;

    @Field("is_active")
    private boolean isActive;

    @Field("created_on")
    private LocalDateTime createdOn;

    @Field("modified_on")
    private LocalDateTime modifiedOn;
}

package vn.hcmute.edu.materialsservice.dtos.request.users;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class CreateUserRequest {

    @NotBlank(message = "userType không được để trống")
    private String userType;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ tên phải từ 2 đến 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải ít nhất 6 ký tự")
    private String password;

    // ===== Field mới — optional =====
    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @URL(message = "Avatar phải là URL hợp lệ")
    private String avatarUrl;

    @Min(value = 1900, message = "Năm sinh không hợp lệ")
    @Max(value = 2026, message = "Năm sinh không được vượt quá năm hiện tại")
    private Integer birthYear;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Giới tính chỉ chấp nhận: MALE, FEMALE, hoặc OTHER")
    private String gender;

}

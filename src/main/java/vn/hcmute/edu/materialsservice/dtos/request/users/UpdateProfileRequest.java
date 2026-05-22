package vn.hcmute.edu.materialsservice.dtos.request.users;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class UpdateProfileRequest {

    // fullname: cho phép null (không bắt buộc gửi), nhưng nếu gửi thì không được blank
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    @Pattern(
            regexp = "^(?!\\s*$).+",           // reject chuỗi chỉ toàn khoảng trắng
            message = "Họ tên không được để trống"
    )
    private String fullname;

    // phoneNumber: nếu gửi lên thì phải đúng format HOẶC gửi null để xóa
    // Regex cho phép null (handled by @Pattern which skips null), reject ""
    @Pattern(
            regexp = "^$|^(0[3|5|7|8|9])+([0-9]{8})$",
            message = "Số điện thoại không hợp lệ (VD: 0912345678)"
    )
    private String phoneNumber;

    // avatarUrl: nếu gửi lên phải là URL hợp lệ hoặc chuỗi rỗng để xóa ảnh
    @URL(message = "Đường dẫn ảnh đại diện phải là một URL hợp lệ")
    private String avatarUrl;

    @Min(value = 1900, message = "Năm sinh không hợp lệ (tối thiểu 1900)")
    @Max(value = 2025, message = "Năm sinh không hợp lệ")
    private Integer birthYear;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    @Pattern(
            regexp = "^(MALE|FEMALE|OTHER)$",
            message = "Giới tính chỉ chấp nhận: MALE, FEMALE, hoặc OTHER"
    )
    private String gender;
}

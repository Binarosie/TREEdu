package vn.hcmute.edu.materialsservice.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 50, message = "Mật khẩu phải từ 6 đến 50 ký tự")
    private String password;

    // Ràng buộc định dạng số điện thoại Việt Nam (Bắt đầu bằng 03, 05, 07, 08, 09 và đủ 10 số)
    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "Số điện thoại không hợp lệ")
    private String phone;

    // Ràng buộc phải là một đường dẫn chuẩn HTTP/HTTPS
    @URL(message = "Đường dẫn ảnh đại diện phải là một URL hợp lệ")
    private String avatarUrl;

    // Ràng buộc tuổi hợp lý (Không quá khứ quá xa, và không vượt quá năm hiện tại là 2026)
    @Min(value = 1900, message = "Năm sinh không hợp lệ")
    @Max(value = 2026, message = "Năm sinh không được vượt quá năm hiện tại")
    private Integer birthYear;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    // Ép Front-end chỉ được gửi 1 trong 3 giá trị Enum dạng chuỗi
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Giới tính chỉ chấp nhận: MALE, FEMALE, hoặc OTHER")
    private String gender;
}

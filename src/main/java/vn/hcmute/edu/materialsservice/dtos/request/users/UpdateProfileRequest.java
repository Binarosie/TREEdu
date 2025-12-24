package vn.hcmute.edu.materialsservice.dtos.request.users;



import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "firstname không được để trống")
    private String fullname;

}
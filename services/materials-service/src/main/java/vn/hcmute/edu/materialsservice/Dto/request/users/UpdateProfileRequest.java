package vn.hcmute.edu.materialsservice.Dto.request.users;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "firstname không được để trống")
    private String fullname;

}
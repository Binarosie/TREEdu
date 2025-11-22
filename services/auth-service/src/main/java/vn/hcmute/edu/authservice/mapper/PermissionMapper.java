package vn.hcmute.edu.authservice.mapper;


import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.hcmute.edu.authservice.dto.CreatePermissionDto;
import vn.hcmute.edu.authservice.dto.PermissionResponse;
import vn.hcmute.edu.authservice.model.Permission;


@Mapper(componentModel = "spring")
public interface PermissionMapper {

    @Mapping(target = "id", ignore = true)
    Permission toEntity(CreatePermissionDto dto);

    PermissionResponse toResponse(Permission permission);

    List<PermissionResponse> toResponseList(List<Permission> permissions);
}
package vn.hcmute.edu.userservice.mapper;

import org.mapstruct.*;
import vn.hcmute.edu.userservice.dto.request.AdminCreateRequest;
import vn.hcmute.edu.userservice.dto.request.AdminUpdateRequest;
import vn.hcmute.edu.userservice.dto.response.AdminResponse;
import vn.hcmute.edu.userservice.model.AdminEntity;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface AdminMapper
        extends BaseMapper<AdminCreateRequest, AdminEntity, AdminResponse> {

    @Override
    AdminEntity toEntity(AdminCreateRequest dto);

    @Override
    AdminResponse toResponse(AdminEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromUpdateRequest(AdminUpdateRequest dto, @MappingTarget AdminEntity entity);
}
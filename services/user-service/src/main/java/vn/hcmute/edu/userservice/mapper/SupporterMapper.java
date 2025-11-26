package vn.hcmute.edu.userservice.mapper;

import org.mapstruct.*;
import vn.hcmute.edu.userservice.dto.request.SupporterCreateRequest;
import vn.hcmute.edu.userservice.dto.request.SupporterUpdateRequest;
import vn.hcmute.edu.userservice.dto.response.SupporterResponse;
import vn.hcmute.edu.userservice.model.SupporterEntity;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface SupporterMapper
        extends BaseMapper<SupporterCreateRequest, SupporterEntity, SupporterResponse> {

    @Override
    SupporterEntity toEntity(SupporterCreateRequest dto);

    @Override
    SupporterResponse toResponse(SupporterEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromUpdateRequest(SupporterUpdateRequest dto, @MappingTarget SupporterEntity entity);
}
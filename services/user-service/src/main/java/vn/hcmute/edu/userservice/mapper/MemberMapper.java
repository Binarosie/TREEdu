package vn.hcmute.edu.userservice.mapper;

import org.mapstruct.*;
import vn.hcmute.edu.userservice.dto.request.MemberCreateRequest;
import vn.hcmute.edu.userservice.dto.request.MemberUpdateRequest;
import vn.hcmute.edu.userservice.dto.response.MemberResponse;
import vn.hcmute.edu.userservice.model.MemberEntity;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface MemberMapper
        extends BaseMapper<MemberCreateRequest, MemberEntity, MemberResponse> {

    @Override
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "userStatus", constant = "ACTIVE")
    @Mapping(target = "level", constant = "BASIC")
        // để xử lý thủ công khi cần nested update
    MemberEntity toEntity(MemberCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromUpdateRequest(MemberUpdateRequest dto, @MappingTarget MemberEntity entity);
}
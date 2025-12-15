//package vn.hcmute.edu.materialsservice.Mapper;
//import org.mapstruct.*;
//import vn.hcmute.edu.materialsservice.Dto.request.RegisterRequest;
//import vn.hcmute.edu.materialsservice.Dto.request.UserRequest;
//import vn.hcmute.edu.materialsservice.Dto.response.UserResponse;
//import vn.hcmute.edu.materialsservice.Model.User;
//
//@Mapper(componentModel = "spring")
//public interface UserMapper {
//
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "roles", constant = "USER")
//    @Mapping(target = "status", constant = "ACTIVE")
//    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
//    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
//    User toEntity(RegisterRequest request);
//
//    UserResponse toResponse(User user);
//
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "roles", ignore = true)
//    @Mapping(target = "status", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
//    @Mapping(target = "password", ignore = true) // sẽ xử lý riêng
//    void updateEntityFromRequest(UserRequest request, @MappingTarget User user);
//}

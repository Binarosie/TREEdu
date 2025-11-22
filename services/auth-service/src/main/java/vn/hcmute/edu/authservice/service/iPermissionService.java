package vn.hcmute.edu.authservice.service;

import vn.hcmute.edu.authservice.model.Permission;

import java.util.List;

public interface iPermissionService {
    Permission create(Permission p);
    List<Permission> listAll();

    Permission getByName(String name);
    Permission updateDescription(String name, String description);
    void deleteByName(String name);
}

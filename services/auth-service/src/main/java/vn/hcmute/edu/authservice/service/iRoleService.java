package vn.hcmute.edu.authservice.service;

import vn.hcmute.edu.authservice.model.Role;

import java.util.List;
import java.util.Set;

public interface iRoleService {
    Role create(Role r);
    Role addPermission(String roleName, String permName);
    Role removePermission(String roleName, String permName);
    Set<String> getPermissions(String roleName);
    List<Role> listAll();
}
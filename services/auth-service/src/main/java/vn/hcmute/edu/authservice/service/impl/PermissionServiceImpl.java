package vn.hcmute.edu.authservice.service.impl;

import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import vn.hcmute.edu.authservice.constants.Messages;
import vn.hcmute.edu.authservice.model.Permission;
import vn.hcmute.edu.authservice.repository.PermissionRepository;
import vn.hcmute.edu.authservice.repository.RoleRepository;
import vn.hcmute.edu.authservice.service.iPermissionService;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements iPermissionService {
    private final PermissionRepository perms;
    private final RoleRepository roles;

    @Override
    public Permission create(Permission p) {
        if (perms.findByName(p.getName()).isPresent()) {
            throw new IllegalArgumentException(String.format(Messages.Error.PERMISSION_ALREADY_EXISTS, p.getName()));
        }
        return perms.save(p);
    }

    @Override
    public List<Permission> listAll() {
        return perms.findAll();
    }

    @Override
    public Permission getByName(String name) {
        return perms.findByName(name)
                .orElseThrow(() -> new NoSuchElementException(Messages.Error.PERMISSION_NOT_FOUND));
    }

    @Override
    public Permission updateDescription(String name, String description) {
        Permission existing = perms.findByName(name)
                .orElseThrow(() -> new NoSuchElementException(Messages.Error.PERMISSION_NOT_FOUND));
        existing.setDescription(description);
        return perms.save(existing);
    }

    @Override
    public void deleteByName(String name) {
        // Prevent destructive delete if any role still references this permission
        if (roles.existsByPermissionsContains(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, Messages.Error.PERMISSION_IN_USE);
        }
        Permission existing = perms.findByName(name)
                .orElseThrow(() -> new NoSuchElementException(Messages.Error.PERMISSION_NOT_FOUND));
        perms.delete(existing);
    }
}
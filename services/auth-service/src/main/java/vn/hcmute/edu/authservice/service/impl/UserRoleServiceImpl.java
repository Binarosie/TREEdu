package vn.hcmute.edu.authservice.service.impl;


import java.util.NoSuchElementException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.authservice.constants.Messages;
import vn.hcmute.edu.authservice.model.User;
import vn.hcmute.edu.authservice.repository.RoleRepository;
import vn.hcmute.edu.authservice.repository.UserRepository;
import vn.hcmute.edu.authservice.service.iUserRoleService;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements iUserRoleService {
    private final UserRepository users;
    private final RoleRepository roles;

    @Override
    @Transactional
    public void grantRoleToUser(String username, String roleName) {
        User u = users.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException(Messages.Error.USER_NOT_FOUND));
        if (roles.findByName(roleName).isEmpty()) {
            throw new NoSuchElementException(Messages.Error.ROLE_NOT_FOUND);
        }
        u.getRoles().add(roleName);
        users.save(u);
    }

    @Override
    @Transactional
    public void revokeRoleFromUser(String username, String roleName) {
        User u = users.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException(Messages.Error.USER_NOT_FOUND));
        u.getRoles().remove(roleName);
        users.save(u);
    }

    @Override
    public Set<String> getUserRoles(String username) {
        return users.findByUsername(username)
                .map(User::getRoles)
                .orElseThrow(() -> new NoSuchElementException(Messages.Error.USER_NOT_FOUND));
    }
}
//package vn.hcmute.edu.materialsservice.config;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import vn.hcmute.edu.materialsservice.Model.User;
//import vn.hcmute.edu.materialsservice.Repository.UserRepository;
//
//import java.util.Collections;
//
//@Service
//@RequiredArgsConstructor
//public class TokenConfig implements UserDetailsService {
//
//    private final UserRepository userRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
//
//        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRoles().name());
//
//        return new org.springframework.security.core.userdetails.User(
//                user.getEmail(),
//                user.getPassword(),
//                Collections.singletonList(authority)
//        );
//    }
//}

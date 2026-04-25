package DBMS.UniSlot.Backend.security;



import DBMS.UniSlot.Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements Spring Security's UserDetailsService.
 * Spring Security calls loadUserByUsername() during authentication
 * to verify credentials against the database.
 *
 * Our "username" is the email address.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load a User entity by email for Spring Security to verify.
     * The User entity implements UserDetails, so it is returned directly.
     *
     * @Transactional ensures the session stays open if Hibernate
     * needs to lazily load any collection on UserDetails.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user found with email: " + email));
    }
}


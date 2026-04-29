package appsembly.appsembly.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import appsembly.appsembly.Repository.UserAppRepository;
import appsembly.appsembly.domain.Roles;
import appsembly.appsembly.domain.UserApp;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserAppRepository userAppRepository;

    // register es el método encargado de guardar un nuevo usuario en la base de
    // datos además se encripta la contraseña del usurio
    @Transactional
    @Override
    public void register(String firstName, String lastName, String email, String password, String passwordConfirm,
            String role, Integer personalCode, Boolean newUser) throws Exception {

        validate(email, password, passwordConfirm);

        if ("USER".equals(role)) {

            UserApp UserApp = new UserApp(null, personalCode, firstName, lastName,
                    new BCryptPasswordEncoder().encode(password), email, newUser,
                    Roles.USER);

            userAppRepository.save(UserApp);
        } else if ("ADMIN".equals(role)) {
            UserApp UserApp = new UserApp(null, personalCode, firstName, lastName,
                    new BCryptPasswordEncoder().encode(password), email, newUser,
                    Roles.ADMIN);
            userAppRepository.save(UserApp);
        }
    }

    // validate hace las comprobaciones necesarias para aceptar los criterios de la
    // contraseña y email
    private void validate(String email, String password, String passwordConfirm) throws Exception {
        if (email == null || email.isEmpty()) {
            throw new Exception("el email no puede ser nulo o estar vacío");

        }

        if (password == null || password.isEmpty() || password.length() < 8) {
            throw new Exception("la contraseña no puede estar vacía, y debe tener al menos 8 carácteres");
        }

        if (!password.equals(passwordConfirm)) {
            throw new Exception("Las contraseñas deben ser iguales");
        }
    }

    // loadUserByUsername carga los detalles de un usuario durante el proceso de
    // autenticación. Cuando un usuario intenta iniciar sesión en la aplicación,
    // Spring Security utiliza este método para obtener los detalles del usuario
    // como los roles
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        UserApp userApp = userAppRepository.findByEmail(email);
        if (userApp != null) {
            List<GrantedAuthority> permiss = new ArrayList<>();
            GrantedAuthority p = new SimpleGrantedAuthority("ROLE_" + userApp.getRole().toString());
            permiss.add(p);
            return new User(userApp.getEmail(), userApp.getPassword(), permiss);
        } else {
            return null;
        }
    }

}

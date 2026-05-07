package appsembly.appsembly.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
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
            log.info("Registering resident user email={} personalCode={}", email, personalCode);

            UserApp userApp = new UserApp();
            userApp.setPersonalCode(personalCode);
            userApp.setFirstName(firstName);
            userApp.setLastName(lastName);
            userApp.setPassword(new BCryptPasswordEncoder().encode(password));
            userApp.setEmail(email);
            userApp.setNewUser(newUser);
            userApp.setRole(Roles.USER);
            userApp.setBlocked(Boolean.FALSE);

            userAppRepository.save(userApp);
        } else if ("ADMIN".equals(role)) {
            log.info("Registering admin user email={} personalCode={}", email, personalCode);

            UserApp userApp = new UserApp();
            userApp.setPersonalCode(personalCode);
            userApp.setFirstName(firstName);
            userApp.setLastName(lastName);
            userApp.setPassword(new BCryptPasswordEncoder().encode(password));
            userApp.setEmail(email);
            userApp.setNewUser(newUser);
            userApp.setRole(Roles.ADMIN);
            userApp.setBlocked(Boolean.FALSE);

            userAppRepository.save(userApp);
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

    @Transactional(readOnly = true)
    @Override
    public List<UserApp> getResidentPadron() {
        return userAppRepository.findAll();
    }

    @Transactional
    @Override
    public UserApp updateResidentHousing(Long userId, String blockName, String towerName, String unitNumber) {
        log.info("Updating housing for user {}", userId);
        UserApp userApp = userAppRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario indicado."));

        // Validar: todo o nada (todos los campos juntos o ninguno)
        boolean hasBlock = isNotEmpty(blockName);
        boolean hasTower = isNotEmpty(towerName);
        boolean hasUnit = isNotEmpty(unitNumber);

        if ((hasBlock || hasTower || hasUnit) && !(hasBlock && hasTower && hasUnit)) {
            throw new IllegalArgumentException("Debe proporcionar bloque, torre y número de unidad juntos, o ninguno.");
        }

        // Si proporciona vivienda, validar que no sea duplicada
        if (hasBlock && hasTower && hasUnit) {
            String normalizedBlock = blockName.trim();
            String normalizedTower = towerName.trim();
            String normalizedUnit = unitNumber.trim();

            // Buscar duplicado (excluyendo el usuario actual)
            userAppRepository.findByHousing(normalizedBlock, normalizedTower, normalizedUnit)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(userId)) {
                            throw new IllegalArgumentException("Ya existe un residente asignado a esta vivienda: " +
                                    normalizedBlock + " - " + normalizedTower + " - " + normalizedUnit);
                        }
                    });

            userApp.setBlockName(normalizedBlock);
            userApp.setTowerName(normalizedTower);
            userApp.setUnitNumber(normalizedUnit);
        } else {
            // Limpiar vivienda si se proporciona nulo
            userApp.setBlockName(null);
            userApp.setTowerName(null);
            userApp.setUnitNumber(null);
        }

        if (userApp.getBlocked() == null) {
            userApp.setBlocked(Boolean.FALSE);
        }

        return userAppRepository.save(userApp);
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Transactional
    @Override
    public UserApp blockResident(Long userId, boolean blocked) {
        log.info("Setting blocked={} for user {}", blocked, userId);
        UserApp userApp = userAppRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario indicado."));

        userApp.setBlocked(blocked);
        return userAppRepository.save(userApp);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserApp> searchResidents(String searchTerm) {
        log.debug("Searching residents with term '{}'", searchTerm);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return userAppRepository.findAll();
        }
        return userAppRepository.searchResidents(searchTerm.trim());
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserApp> findResidentsByBlock(String blockName) {
        if (blockName == null || blockName.trim().isEmpty()) {
            return List.of();
        }
        return userAppRepository.findByBlock(blockName.trim());
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserApp> findResidentsByTower(String towerName) {
        if (towerName == null || towerName.trim().isEmpty()) {
            return List.of();
        }
        return userAppRepository.findByTower(towerName.trim());
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

    private String normalize(String value) {
        return value == null ? null : value.trim().isEmpty() ? null : value.trim();
    }

}

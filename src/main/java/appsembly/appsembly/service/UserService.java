package appsembly.appsembly.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import appsembly.appsembly.domain.UserApp;

public interface UserService extends UserDetailsService {
    void register(String firstName, String lastName, String email, String password, String passwordConfirm,
            String role, Integer personalCode, Boolean newUser) throws Exception;

    List<UserApp> getResidentPadron();

    UserApp updateResidentHousing(Long userId, String blockName, String towerName, String unitNumber);

    UserApp blockResident(Long userId, boolean blocked);

    List<UserApp> searchResidents(String searchTerm);

    List<UserApp> findResidentsByBlock(String blockName);

    List<UserApp> findResidentsByTower(String towerName);
}

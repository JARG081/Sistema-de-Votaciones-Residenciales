package appsembly.appsembly.service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import appsembly.appsembly.Repository.UserAppRepository;
import appsembly.appsembly.domain.Roles;
import appsembly.appsembly.domain.UserApp;

@Service
public class DataSeedService {
    private final UserAppRepository userAppRepository;

    public DataSeedService(UserAppRepository userAppRepository) {
        this.userAppRepository = userAppRepository;
    }

    @Transactional
    public SeedSummary seedResidentData(String actor) {
        List<SeedResident> residents = List.of(
                new SeedResident("Laura", "Perez", "laura.perez@demo.local", 2001, "Bloque A", "Torre 1", "101"),
                new SeedResident("Miguel", "Rojas", "miguel.rojas@demo.local", 2002, "Bloque A", "Torre 2", "202"),
                new SeedResident("Sofia", "Lopez", "sofia.lopez@demo.local", 2003, "Bloque B", "Torre 1", "103"),
                new SeedResident("Andres", "Vega", "andres.vega@demo.local", 2004, "Bloque B", "Torre 2", "204"));

        int created = 0;
        int existing = 0;
        for (SeedResident resident : residents) {
            if (userAppRepository.findByEmail(resident.email()) != null) {
                existing++;
                continue;
            }

            UserApp userApp = new UserApp();
            userApp.setFirstName(resident.firstName());
            userApp.setLastName(resident.lastName());
            userApp.setEmail(resident.email());
            userApp.setPersonalCode(resident.personalCode());
            userApp.setPassword(new BCryptPasswordEncoder().encode("ChangeMe123!"));
            userApp.setNewUser(Boolean.FALSE);
            userApp.setRole(Roles.USER);
            userApp.setBlocked(Boolean.FALSE);
            userApp.setBlockName(resident.blockName());
            userApp.setTowerName(resident.towerName());
            userApp.setUnitNumber(resident.unitNumber());
            userAppRepository.save(userApp);
            created++;
        }

        return new SeedSummary(actor, created, existing, residents.size());
    }

    public record SeedSummary(String actor, int created, int existing, int total) {
    }

    private record SeedResident(String firstName, String lastName, String email, Integer personalCode,
            String blockName, String towerName, String unitNumber) {
    }
}
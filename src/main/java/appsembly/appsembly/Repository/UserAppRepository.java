package appsembly.appsembly.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import appsembly.appsembly.domain.UserApp;

@Repository
public interface UserAppRepository extends JpaRepository<UserApp, Long> {

    // findByEmail sirve para recuperar un usuario de la base de datos usando el
    // email
    @Query("SELECT u FROM UserApp u WHERE u.email = :email")
    UserApp findByEmail(@Param("email") String email);

    Optional<UserApp> findByPersonalCode(Integer personalCode);

    List<UserApp> findAllByBlockedFalse();

    // Busca por vivienda (block, tower, unit) para validar duplicados
    @Query("SELECT u FROM UserApp u WHERE u.blockName = :blockName AND u.towerName = :towerName AND u.unitNumber = :unitNumber")
    Optional<UserApp> findByHousing(@Param("blockName") String blockName, @Param("towerName") String towerName, @Param("unitNumber") String unitNumber);

    // Búsqueda por nombre, email, bloque o torre para administración
    @Query("SELECT u FROM UserApp u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.blockName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.towerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.unitNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<UserApp> searchResidents(@Param("searchTerm") String searchTerm);

    // Filtro por bloque
    @Query("SELECT u FROM UserApp u WHERE u.blockName = :blockName ORDER BY u.towerName, u.unitNumber")
    List<UserApp> findByBlock(@Param("blockName") String blockName);

    // Filtro por torre
    @Query("SELECT u FROM UserApp u WHERE u.towerName = :towerName ORDER BY u.blockName, u.unitNumber")
    List<UserApp> findByTower(@Param("towerName") String towerName);
}

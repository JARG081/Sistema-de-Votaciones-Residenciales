package appsembly.appsembly.Repository;

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
}

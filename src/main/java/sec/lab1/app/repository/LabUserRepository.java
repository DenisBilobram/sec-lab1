package sec.lab1.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sec.lab1.app.model.LabUser;

public interface LabUserRepository extends JpaRepository<LabUser, Long> {
    Optional<LabUser> findByUsername(String username);
}

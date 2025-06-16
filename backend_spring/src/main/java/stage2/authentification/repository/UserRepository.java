package stage2.authentification.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import stage2.authentification.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
}
package it.uniroma3.siw.repository;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CredentialsRepository extends CrudRepository<Credentials, Long> {
    Optional<Credentials> findByUsername(String username);
    Optional<Credentials> findByUser(User user);
    List<Credentials> findByRole(String role);
}

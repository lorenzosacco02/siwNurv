package it.uniroma3.siw.repository;
import it.uniroma3.siw.model.Acquirente;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface AcquirenteRepository extends CrudRepository<Acquirente,Long>{
    //metodo che serve quando il bot legge il comando "/collega NURV-1234"
    Optional<Acquirente> findByCodiceAssociazione(String codiceAssociazione);
}

package co.edu.uniquindio.huellitas.repository;

import co.edu.uniquindio.huellitas.entity.Animal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long> {
}

package ru.msu.cmc.webprak.repository;

import ru.msu.cmc.webprak.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {
}


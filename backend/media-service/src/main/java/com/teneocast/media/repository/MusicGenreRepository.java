package com.teneocast.media.repository;

import com.teneocast.media.entity.MusicGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MusicGenreRepository extends JpaRepository<MusicGenre, Long> {
    
    Optional<MusicGenre> findByName(String name);
    
    boolean existsByName(String name);
}

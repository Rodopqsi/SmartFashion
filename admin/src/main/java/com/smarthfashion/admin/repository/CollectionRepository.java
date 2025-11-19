package com.smarthfashion.admin.repository;

import com.smarthfashion.admin.domain.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findAllByOrderByOrdenAscIdDesc();
}

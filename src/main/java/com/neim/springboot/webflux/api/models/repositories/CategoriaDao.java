package com.neim.springboot.webflux.api.models.repositories;

import com.neim.springboot.webflux.api.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {
    public Mono<Categoria> findByNombre(String nombre);
}

package com.neim.springboot.webflux.api.services;

import com.neim.springboot.webflux.api.models.documents.Categoria;
import com.neim.springboot.webflux.api.models.documents.Producto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoService {

    public Flux<Producto> findAll();

    public Flux<Producto> findAllNameWithUpperCase();

    public Flux<Producto> findAllNameWithUpperCaseRepeat();

    public Mono<Producto> findById(String id);

    public Mono<Producto> save(Producto producto);

    public Mono<Void> delete(Producto producto);

    public Flux<Categoria> findAllByCategoria();

    public Mono<Categoria> findByCategoriaById(String id);

    public Mono<Categoria> saveCategoria(Categoria categoria);

    public Mono<Producto> findByNombre(String nombre);

    public Mono<Categoria> findCategoriaByNombre(String nombre);

}

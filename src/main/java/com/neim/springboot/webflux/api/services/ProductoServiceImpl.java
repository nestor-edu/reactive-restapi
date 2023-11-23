package com.neim.springboot.webflux.api.services;

import com.neim.springboot.webflux.api.models.documents.Categoria;
import com.neim.springboot.webflux.api.models.documents.Producto;
import com.neim.springboot.webflux.api.models.repositories.CategoriaDao;
import com.neim.springboot.webflux.api.models.repositories.ProductoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductoServiceImpl implements ProductoService{

    @Autowired
    private ProductoDao dao;

    @Autowired
    private CategoriaDao categoriaDao;

    @Override
    public Flux<Producto> findAll() {
        return dao.findAll();
    }

    @Override
    public Flux<Producto> findAllNameWithUpperCase() {
        return dao.findAll().map(producto -> {
            producto.setNombre(producto.getNombre().toUpperCase());
            return producto;
        });
    }

    @Override
    public Flux<Producto> findAllNameWithUpperCaseRepeat() {
        return findAllNameWithUpperCase().repeat(5000);
    }

    @Override
    public Mono<Producto> findById(String id) {
        return dao.findById(id);
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return dao.save(producto);
    }

    @Override
    public Mono<Void> delete(Producto producto) {
        return dao.delete(producto);
    }

    @Override
    public Flux<Categoria> findAllByCategoria() {
        return categoriaDao.findAll();
    }

    @Override
    public Mono<Categoria> findByCategoriaById(String id) {
        return categoriaDao.findById(id);
    }

    @Override
    public Mono<Categoria> saveCategoria(Categoria categoria) {
        return categoriaDao.save(categoria);
    }

    @Override
    public Mono<Producto> findByNombre(String nombre) {
        return dao.obtenerNombre(nombre);
    }

    @Override
    public Mono<Categoria> findCategoriaByNombre(String nombre) {
        return categoriaDao.findByNombre(nombre);
    }
}

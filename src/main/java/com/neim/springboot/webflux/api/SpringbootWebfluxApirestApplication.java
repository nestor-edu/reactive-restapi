package com.neim.springboot.webflux.api;

import com.neim.springboot.webflux.api.models.documents.Categoria;
import com.neim.springboot.webflux.api.models.documents.Producto;
import com.neim.springboot.webflux.api.services.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;


@SpringBootApplication
public class SpringbootWebfluxApirestApplication implements CommandLineRunner {

    @Autowired
    private ProductoService service;

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    private static final Logger log = LoggerFactory.getLogger(SpringbootWebfluxApirestApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringbootWebfluxApirestApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        mongoTemplate.dropCollection("productos").subscribe();
        mongoTemplate.dropCollection("categorias").subscribe();

        Categoria cellphones = new Categoria("Cellphones");
        Categoria electronics = new Categoria("Accessories");
        Categoria computers = new Categoria("Computers & Tablets");
        Categoria video = new Categoria("TV, Video & Home Audio");

        Flux.just(cellphones, electronics, computers, video).flatMap(c -> service.saveCategoria(c)).doOnNext(categoria -> {
            log.info("Categoria creada: " + categoria.getNombre() + " ID: " + categoria.getId());
        }).thenMany(Flux.just(new Producto("iPhone 12 Pro Max", 456.84, cellphones),
                                new Producto("TV Samsung 4k 42", 568.58, video),
                                new Producto("Sony Camara HD Digital", 177.89, electronics),
                                new Producto("HP Notebook", 46.89, computers),
                                new Producto("Macbook Air Pro", 1299.99, computers),
                                new Producto("Apple iPad Air 3rd gen", 187.55, computers)
                        )
                        .flatMap(producto -> {
                            producto.setCreateAt(new Date());
                            return service.save(producto);
                        })
        ).subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNombre()));
    }
}

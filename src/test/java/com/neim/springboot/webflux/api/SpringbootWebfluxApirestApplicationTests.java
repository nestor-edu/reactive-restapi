package com.neim.springboot.webflux.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neim.springboot.webflux.api.models.documents.Categoria;
import com.neim.springboot.webflux.api.models.documents.Producto;
import com.neim.springboot.webflux.api.services.ProductoService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SpringbootWebfluxApirestApplicationTests {

    @Autowired
    private WebTestClient client;

    @Autowired
    private ProductoService service;

    @Value("${config.base.endpoint}")
    private String url;

    @Test
    void contextLoads() {
        client.get().uri(url).accept(MediaType.APPLICATION_JSON).exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Producto.class)
                .consumeWith(response -> {
                    List<Producto> productoList = response.getResponseBody();
                    productoList.forEach(producto -> {
                        System.out.println(producto.getNombre());
                    });

                    Assertions.assertThat(productoList.size() > 0).isTrue();
                });
                // .hasSize(6);
    }

    @Test
    public void verTest() {
        Producto productoMono = service.findByNombre("iPhone 12 Pro Max").block();

        client.get()
                .uri(url + "/{id}", Collections.singletonMap("id", productoMono.getId()))
                .accept(MediaType.APPLICATION_JSON).exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Producto.class)
                .consumeWith(response -> {
                    Producto productoList = response.getResponseBody();
                    Assertions.assertThat(productoList.getId()).isNotEmpty();
                    Assertions.assertThat(productoList.getId().length() > 0).isTrue();
                    Assertions.assertThat(productoList.getNombre()).isEqualTo("iPhone 12 Pro Max");
                });
                /*.expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.nombre").isEqualTo("iPhone 12 Pro Max"); */
    }

    @Test
    public void crearTest() {
        Categoria categoria = service.findCategoriaByNombre("Accessories").block();
        Producto producto = new Producto("Escritorio Gamer RTX", 350.00, categoria);

        client.post().uri(url).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(producto), Producto.class)
                .exchange().expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.producto.id").isNotEmpty()
                .jsonPath("$.producto.nombre").isEqualTo("Escritorio Gamer RTX")
                .jsonPath("$.producto.categoria.nombre").isEqualTo("Accessories");
    }

    @Test
    public void crear2Test() {
        Categoria categoria = service.findCategoriaByNombre("Accessories").block();
        Producto producto = new Producto("Escritorio Gamer RTX", 350.00, categoria);

        client.post().uri(url).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(producto), Producto.class)
                .exchange().expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {})
                .consumeWith(response -> {
                    Object o = response.getResponseBody().get("producto");
                    Producto p = new ObjectMapper().convertValue(o, Producto.class);
                    Assertions.assertThat(p.getId()).isNotEmpty();
                    Assertions.assertThat(p.getNombre()).isEqualTo("Escritorio Gamer RTX");
                    Assertions.assertThat(p.getCategoria().getNombre()).isEqualTo("Accessories");
                });
    }

    @Test
    public void editarTest() {
        Producto producto = service.findByNombre("Macbook Air Pro").block();
        Categoria categoria = service.findCategoriaByNombre("Computers & Tablets").block();

        Producto productoEditado = new Producto("MacBook Air M1 2022", 1350.00, categoria);

        client.put().uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(productoEditado), Producto.class)
                .exchange().expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().jsonPath("$.id").isNotEmpty()
                .jsonPath("$.nombre").isEqualTo("MacBook Air M1 2022");
    }

    @Test
    public void eliminarTest() {
        Producto producto = service.findByNombre("HP Notebook").block();
        client.delete().uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
                .exchange().expectStatus().isNoContent().expectBody().isEmpty();

        client.get().uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
                .exchange().expectStatus().isNotFound().expectBody().isEmpty();
    }

}

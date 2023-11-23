package com.neim.springboot.webflux.api.handler;

import com.neim.springboot.webflux.api.models.documents.Categoria;
import com.neim.springboot.webflux.api.models.documents.Producto;
import com.neim.springboot.webflux.api.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

@Component public class ProductoHandler {

    @Autowired
    private ProductoService service;

    @Value("${config.uploads.path}") private String path;

    @Autowired private Validator validator;

    public Mono<ServerResponse> upload(ServerRequest request) {
        String id = request.pathVariable("id");
        return  request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class).flatMap(filePart -> service.findById(id)
                        .flatMap(producto -> {
                            producto.setFoto(UUID.randomUUID().toString() + "-" + filePart.filename()
                                    .replace(" ", "-")
                                    .replace(":", "")
                                    .replace("\\", ""));
                            return filePart.transferTo(new File(path + producto.getFoto()))
                                    .then(service.save(producto));
                        })).flatMap(p -> ServerResponse.created(URI.create("api/v2/productos/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p)).switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> crearConFoto(ServerRequest request) {
        Mono<Producto> productoMono = request.multipartData().map(multipart -> {
            FormFieldPart nombre = (FormFieldPart) multipart.toSingleValueMap().get("nombre");
            FormFieldPart precio = (FormFieldPart) multipart.toSingleValueMap().get("precio");
            FormFieldPart categoriaId = (FormFieldPart) multipart.toSingleValueMap().get("categoria.id");
            FormFieldPart categoriaNombre = (FormFieldPart) multipart.toSingleValueMap().get("categoria.nombre");

            Categoria categoria = new Categoria(categoriaNombre.value());
            categoria.setId(categoriaId.value());
            return new Producto(nombre.value(), Double.parseDouble(precio.value()), categoria);
        });
        return  request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class).flatMap(filePart -> productoMono
                        .flatMap(producto -> {
                            producto.setFoto(UUID.randomUUID().toString() + "-" + filePart.filename()
                                    .replace(" ", "-")
                                    .replace(":", "")
                                    .replace("\\", ""));
                            producto.setCreateAt(new Date());
                            return filePart.transferTo(new File(path + producto.getFoto()))
                                    .then(service.save(producto));
                        })).flatMap(p -> ServerResponse.created(URI.create("api/v2/productos/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p));
    }

    public Mono<ServerResponse> listar(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(service.findAll(), Producto.class);
    }

    public Mono<ServerResponse> verDetalle(ServerRequest request) {
        String id = request.pathVariable("id");

        return service.findById(id).flatMap(producto -> ServerResponse
                .ok().contentType(MediaType.APPLICATION_JSON)
                .bodyValue(producto))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> crear(ServerRequest request) {
        Mono<Producto> productoMono = request.bodyToMono(Producto.class);
        return productoMono.flatMap(producto -> {
            Errors errors = new BeanPropertyBindingResult(producto, Producto.class.getName());
            validator.validate(producto, errors);
            if (errors.hasErrors()) {
                return Flux.fromIterable(errors.getFieldErrors()).map(
                        fieldError -> "Campo " + fieldError.getField() + " " + fieldError.getDefaultMessage()
                ).collectList().flatMap(list -> ServerResponse.badRequest().bodyValue(list));
            } else {
                if (producto.getCreateAt() == null) {
                    producto.setCreateAt(new Date());
                }
                return service.save(producto).flatMap(pdb -> ServerResponse.created(URI.create("api/v2/productos/".concat(pdb.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(pdb));
            }
        });
    }

    public Mono<ServerResponse> editar(ServerRequest request) {
        Mono<Producto> productoMono = request.bodyToMono(Producto.class);
        String id = request.pathVariable("id");

        Mono<Producto> productoDB = service.findById(id);

        return productoDB.zipWith(productoMono, (db, req) -> {
            db.setNombre(req.getNombre());
            db.setPrecio(req.getPrecio());
            db.setCategoria(req.getCategoria());
            return db;
        }).flatMap(producto -> ServerResponse.created(URI.create("api/v2/productos/".concat(producto.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.save(producto), Producto.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> eliminar(ServerRequest request) {
        String id = request.pathVariable("id");

        Mono<Producto> productoDB = service.findById(id);

        return productoDB.flatMap(producto -> service.delete(producto).then(ServerResponse.noContent().build()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}

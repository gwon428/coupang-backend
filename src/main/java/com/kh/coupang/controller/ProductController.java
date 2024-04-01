package com.kh.coupang.controller;

import com.kh.coupang.domain.Product;
import com.kh.coupang.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService service;

    @PostMapping("/product")
    public ResponseEntity<Product> create(@RequestBody Product vo) {
        Product result = service.create(vo);
        if (result != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }
        // 보내는 것이 없기 때문에 build()로 리턴
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/product")
    public ResponseEntity<List<Product>> viewAll(@RequestParam(name="category", required = false) Integer category) {
        List<Product> list = service.viewAll();
        return category==null? ResponseEntity.status(HttpStatus.OK).body(list) : ResponseEntity.status(HttpStatus.OK).body(service.viewCategory(category));
    }

    @GetMapping("/product/{code}")
    public ResponseEntity<Product> view(@PathVariable(name = "code") int code) {
        Product result = service.view(code);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PutMapping("/product")
    public ResponseEntity<Product> update(@RequestBody Product vo) {
        Product target = service.update(vo);
        // 삼항연산자 활용
        // 보내는 게 없을 때는 body()가 아닌 build()로
        return (target != null) ? ResponseEntity.status(HttpStatus.ACCEPTED).body(target) : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @DeleteMapping("/product/{code}")
    public ResponseEntity<Product> delete(@PathVariable("code") int code) {
        Product target = service.delete(code);
        return (target != null) ? ResponseEntity.status(HttpStatus.ACCEPTED).body(target) : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }


}

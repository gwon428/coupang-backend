package com.kh.coupang.controller;

import com.kh.coupang.domain.Category;
import com.kh.coupang.domain.Product;
import com.kh.coupang.domain.ProductDTO;
import com.kh.coupang.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService service;

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    @PostMapping("/product")
    public ResponseEntity<Product> create(ProductDTO dto) throws IOException {

        // 파일 업로드
        String fileName = dto.getFile().getOriginalFilename();
        //UUID : 랜덤값
        String uuid = UUID.randomUUID().toString();
        String saveName = uploadPath + File.separator  + "product" + File.separator + uuid +"_" + fileName;
        Path savePath = Paths.get(saveName);
        dto.getFile().transferTo(savePath);     // 파일 업로드가 실제로 일어나고 있음!

        // Product vo 값들 담아서
        Product vo = new Product();
        vo.setProdName(dto.getProdName());
        vo.setProdCode(dto.getPrice());
        vo.setProdPhoto(saveName);

        Category category = new Category();
        category.setCateCode(dto.getCateCode());
        vo.setCategory(category);

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
    public ResponseEntity<Product> update(ProductDTO dto) throws IOException {

        Product vo = new Product();
        vo.setProdCode(dto.getProdCode());
        vo.setProdName(dto.getProdName());
        vo.setPrice(dto.getPrice());

        Category category = new Category();
        category.setCateCode(dto.getCateCode());
        vo.setCategory(category);

//        log.info("empty? : " + dto.getFile().isEmpty());
        Product prev = service.view(dto.getProdCode());

        if(prev.getProdPhoto() == null){
            if(!dto.getFile().isEmpty()){
                // 기존 사진이 없는 경우 + 추가하는 사진이 있는 경우
                // 새로운 사진 추가 (파일 업로드)
                String fileName = dto.getFile().getOriginalFilename();
                //UUID : 랜덤값
                String uuid = UUID.randomUUID().toString();
                String saveName = uploadPath + File.separator + "product" + File.separator + uuid + "_" + fileName;
                Path savePath = Paths.get(saveName);
                dto.getFile().transferTo(savePath);

                // vo에 파일 담기
                vo.setProdPhoto(saveName);
            }
        } else {
            if(dto.getFile().isEmpty()){
                // 기존 사진이 있고, 추가하는 사진이 없는 경우
                // 만약 새로운 사진이 없는 경우 기존 사진 경로 그대로 vo로 담아내야 한다.
                vo.setProdPhoto(prev.getProdPhoto());
            } else {
                // 파일 수정
                // 기존 사진은 삭제하고, 새로운 사진을 추가 (새로운 사진이 없는 경우 그냥 삭제만)
                // 기존 정보 가져오기
                // 기존 사진 삭제하고
                File file = new File(prev.getProdPhoto());

                // 새로운 사진 추가 (파일 업로드)
                String fileName = dto.getFile().getOriginalFilename();
                //UUID : 랜덤값
                String uuid = UUID.randomUUID().toString();
                String saveName = uploadPath + File.separator + "product" + File.separator + uuid + "_" + fileName;
                Path savePath = Paths.get(saveName);
                dto.getFile().transferTo(savePath);

                // vo에 파일 담기
                vo.setProdPhoto(saveName);
            }
        }

//        if(dto.getFile().isEmpty() && prev.getProdPhoto() != null) {
//                // 기존 사진이 있고, 추가하는 사진이 없는 경우
//              // 만약 새로운 사진이 없는 경우 기존 사진 경로 그대로 vo로 담아내야 한다.
//              vo.setProdPhoto(prev.getProdPhoto());
//        } else if (!dto.getFile().isEmpty() && prev.getProdPhoto() == null) {
//            // 기존 사진이 없는 경우 + 추가하는 사진이 있는 경우
//            // 새로운 사진 추가 (파일 업로드)
//            String fileName = dto.getFile().getOriginalFilename();
//            //UUID : 랜덤값
//            String uuid = UUID.randomUUID().toString();
//            String saveName = uploadPath + File.separator + "product" + File.separator + uuid + "_" + fileName;
//            Path savePath = Paths.get(saveName);
//            dto.getFile().transferTo(savePath);
//
//            // vo에 파일 담기
//            vo.setProdPhoto(saveName);
//        } else if (dto.getFile().isEmpty() && prev.getProdPhoto() == null){
//            // 기존 사진이 없고, 추가하는 사진이 없는 경우
//
//        } else {
//                // 파일 수정
//                // 기존 사진은 삭제하고, 새로운 사진을 추가 (새로운 사진이 없는 경우 그냥 삭제만)
//                // 기존 정보 가져오기
//                // 기존 사진 삭제하고
//                File file = new File(prev.getProdPhoto());
//
//                // 새로운 사진 추가 (파일 업로드)
//                String fileName = dto.getFile().getOriginalFilename();
//                //UUID : 랜덤값
//                String uuid = UUID.randomUUID().toString();
//                String saveName = uploadPath + File.separator + "product" + File.separator + uuid + "_" + fileName;
//                Path savePath = Paths.get(saveName);
//                dto.getFile().transferTo(savePath);
//
//                // vo에 파일 담기
//                vo.setProdPhoto(saveName);
//            }

        Product target = service.update(vo);
        // 삼항연산자 활용
        // 보내는 게 없을 때는 body()가 아닌 build()로
        return (target != null) ? ResponseEntity.status(HttpStatus.ACCEPTED).body(target) : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @DeleteMapping("/product/{code}")
    public ResponseEntity<Product> delete(@PathVariable("code") int code) {

        // 파일 있을 경우 삭제 로직
        Product prev = service.view(code);
        File file = new File(prev.getProdPhoto());
        if(file != null){
            file.delete();
        }

        Product target = service.delete(code);
        return (target != null) ? ResponseEntity.status(HttpStatus.ACCEPTED).body(target) : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}

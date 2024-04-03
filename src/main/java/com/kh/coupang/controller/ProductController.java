package com.kh.coupang.controller;

import com.kh.coupang.domain.Category;
import com.kh.coupang.domain.Product;
import com.kh.coupang.domain.ProductDTO;
import com.kh.coupang.domain.QProduct;
import com.kh.coupang.service.ProductService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<List<Product>> viewAll(@RequestParam(name="category", required = false) Integer category, @RequestParam (name="page", defaultValue = "1") int page) {
        // Sort.by : 정렬 기준 설정, descending : desc
        Sort sort = Sort.by("prodCode").descending();
        Pageable pageable = PageRequest.of(page-1, 10,  sort);

        // QueryDSL
        // 1. 가장 먼저 동적 처리하기 위한 Q도메인 클래스 얻어오기
        // Q 도메인 클래스를 이용하면 Entity 클래스에 선언된 필드들을 변수로 활용할 수 있음
        QProduct qProduct = QProduct.product;
        // 2. WHERE 조건을 쓰기 위해서는 BooleanBuilder가 필요
        // BooleanBuilder : where문에 들어가는 조건들을 넣어주는 컨테이너
        BooleanBuilder builder = new BooleanBuilder();
        // category의 유무 여부가 상황에 따라 다르기 때문에 조건이 필요
        if(category != null){
            // 3. 원하는 조건을 필드값과 같이 결합해서 생성
            BooleanExpression expression = qProduct.category.cateCode.eq(category);

            // 4. 만들어진 조건은 where문에 and나 or 같은 키워드와 결합
            builder.and(expression);
        }

        // 5. BooleanBuilder는 QuerydslPredicateExcutor 인터페이스의 findAll() 사용
        Page<Product> list = service.viewAll(builder, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(list.getContent());
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

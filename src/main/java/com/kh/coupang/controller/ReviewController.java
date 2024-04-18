package com.kh.coupang.controller;

import com.kh.coupang.domain.*;
import com.kh.coupang.service.ReviewCommentService;
import com.kh.coupang.service.ReviewService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequestMapping("/api/*")
@RestController
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class ReviewController {

    @Autowired
    private ReviewService service;

    @Autowired
    private ReviewCommentService comment;

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    @PostMapping("/review")
    public ResponseEntity<Review> create(ReviewDTO dto) throws IOException {

        // review부터 추가하여 revi_code가 담긴 review 생성
        Review vo = new Review();

        // 사용자에게 받는 정보 dto로 받아 review에 담음
        vo.setId(dto.getId());
        vo.setProdCode(dto.getProdCode());
        vo.setReviTitle(dto.getReviTitle());
        vo.setReviDesc(dto.getReviDesc());
        vo.setRating(dto.getRating());

        // review 객체 생성
        Review result = service.create(vo);

        if (dto.getFiles() != null) {
            // review_image에는 revi_code가 필요
            for (MultipartFile file : dto.getFiles()) {
                ReviewImage img = new ReviewImage();

                String fileName = file.getOriginalFilename();
                String uuid = UUID.randomUUID().toString();
                String saveName = uploadPath + File.separator + "review" + File.separator + uuid + "_" + fileName;

                Path savePath = Paths.get(saveName);
                file.transferTo(savePath);

                img.setReviUrl(saveName);
                img.setReview(result);
                service.createImg(img);
            }
        }
        return result != null ? ResponseEntity.status(HttpStatus.CREATED).body(result) :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    // 리뷰 수정 기능
    @PutMapping("/review")
    public ResponseEntity update(ReviewDTO dto) throws IOException {

        log.info("dto : " + dto);
        // 리뷰 코드 가지고 와서 그 전에 파일이 있고 없고에 따라 이미지 처리

        // 1. 기존 리뷰에 있던 이미지들 정보 가져오기
        List<ReviewImage> list = service.viewImges(dto.getReviCode());

        for(ReviewImage image : list){
            if((dto.getImages()!=null && !dto.getImages().contains(image.getReviUrl()))||dto.getImages()==null){
                File file = new File(image.getReviUrl());
                file.delete();

                service.deleteImg(image.getReviImgCode());
                log.info("삭제!");
            }

        }

        if (dto.getFiles() != null) {
            // review_image에는 revi_code가 필요
            for (MultipartFile file : dto.getFiles()) {
                ReviewImage img = new ReviewImage();

                String fileName = file.getOriginalFilename();
                String uuid = UUID.randomUUID().toString();
                String saveName = uploadPath + File.separator + "review" + File.separator + uuid + "_" + fileName;

                Path savePath = Paths.get(saveName);
                file.transferTo(savePath);

                img.setReviUrl(saveName);
                // review에 builder 추가
                img.setReview(
                        Review.builder()
                                .reviCode(dto.getReviCode())
                                .build());

                service.createImg(img);
            }
        }

    // dto.images : 추가되는 이미지들이 String 형태로 => 삭제하지 않은 사진들
    //              없으면 기존 사진 삭제한 것들
    // 1. 기존 리뷰에 있던 이미지들 정보 가져오기
    // 2. 반복문 돌려서 dto.images에 해당 이미지가 포함되어 있는지 판단
    // (삭제되지 않은 이미지) dto.getImages().contains(image.getReviUrl()) <-- 이거 사용!
//        log.info(dto.getImages().contains(image.getReviUrl()));
    // 3. 위의 코드로 조건을 걸어서 파일 삭제
        
    // 4. 파일 삭제와 동시에 테이블에서도 해당 정보 삭제
    // ------- 기존 이미지 처리 ------
    // 리뷰 수정
        Review vo = Review.builder()
                .reviCode(dto.getReviCode())
                .id(dto.getId())
                .prodCode(dto.getProdCode())
                .reviTitle(dto.getReviTitle())
                .reviDesc(dto.getReviDesc())
                        .build();
        service.create(vo);
        return ResponseEntity.ok().build();
    }

    // 리뷰 삭제 기능
    @DeleteMapping("/review/{code}")
    public ResponseEntity delete(@PathVariable(name="code") int code){
        // code : 리뷰 코드
        // 이미지 삭제 + 리뷰 이미지 테이블에서도 삭제
        // 1. 이미지 테이블에서 해당 reviCode에 관한 이미지들 가지고 오기 (List<ReviewImage>)
        // --> SELECT 문 생각해보고 DAO에 추가해서 service 반영
        List<ReviewImage> list = service.viewImges(code);

        if(list != null) {
            // 2. 반복문 돌려서 각각의 image에 있는 URL로 file 객체 file.delete() 사용
            for (ReviewImage item : list) {
                File file = new File(item.getReviUrl());
                file.delete();

                service.deleteImg(item.getReviImgCode());
            }
            // 3. 반복문 안에서 이미지 테이블에서 이미지 code로 삭제 기능 진행

        }
        // 리뷰 삭제
        service.delete(code);
        return null;
    }

    // http://localhost:8080/api/public/product/72/review
    // 상품 1개에 따른 리뷰 전체 보기
    @GetMapping("/public/product/{code}/review")
    public ResponseEntity<List<Review>> viewAll(@PathVariable(name="code") int code, @RequestParam(name="page", defaultValue = "1") int page){
        Sort sort = Sort.by("reviCode").descending();
        Pageable pageable = PageRequest.of(page-1, 10, sort);

        QReview qReview = QReview.review;
        BooleanBuilder builder = new BooleanBuilder();

        BooleanExpression expression = qReview.prodCode.eq(code);
        builder.and(expression);
//        if(code != null){
//            BooleanExpression expression = qReview.prodCode.eq(code);
//            builder.and(expression);
//        }

        Page<Review> list = service.viewAll(pageable, builder);
        return ResponseEntity.status(HttpStatus.OK).body(list.getContent());
    }

    public Object authentication(){
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        return authentication.getPrincipal();


    }
    // 리뷰 댓글 추가
    @PostMapping("/review/comment")
    public ResponseEntity createComment(@RequestBody ReviewComment vo){

        Object principal = authentication();

        if(principal instanceof Member){
            Member user = (Member) principal;
            vo.setUser(user);
            return ResponseEntity.ok(comment.create(vo));
        }
        return ResponseEntity.badRequest().build();
    }

    // 상품 1개에 따른 댓글 조회
    @GetMapping("/public/review/{code}/comment")
    public ResponseEntity<List<ReviewCommentDTO>> viewComment(@PathVariable("code") int code){
        // 상위 댓글 리스트
        List<ReviewComment> topList = comment.getTopLevelComments(code);

        // 상위 댓글을 DTO를 통해 가공
        List<ReviewCommentDTO> response = new ArrayList<>();

        // ArrayList에 반복문을 통해 가공된 상위 댓글 넣기
        for(ReviewComment top : topList){

            // 각각 상위 댓글의 revi_code를 통해 하위 댓글 리스트 가져오기
            List<ReviewComment> replies = comment.getRepliesComments(top.getReviCode(), code);

            // 하위 댓글을 DTO를 통해 가공
            List<ReviewCommentDTO> repliesDTO = new ArrayList<>();

            // ArrayList에 반복문을 통해 가공된 하위 댓글 넣기
            for(ReviewComment reply : replies){
                ReviewCommentDTO dto = ReviewCommentDTO.builder()
                        .reviCode(reply.getReviComCode())
                        .reviComCode(reply.getReviComCode())
                        .reviComDesc(reply.getReviComDesc())
                        .reviComDate(reply.getReviComDate())
                        // 댓글에 저장된 user 정보 가져오려면 getUser를 통해 한 번 더 들어가서 정보를 가져와야 함
                        .user(UserDTO.builder()
                                .id(reply.getUser().getId())
                                .name(reply.getUser().getName())
                                .build())
                        .build();
                repliesDTO.add(dto);
            }

            // ArrayList에 반복문을 통해 가공된 상위 댓글 넣기 (이 때, (하위 댓글 리스트)repliesDTO list도 넣게 됨)
            ReviewCommentDTO dto = ReviewCommentDTO.builder()
                    .reviCode(top.getReviCode())
                    .reviComCode(top.getReviComCode())
                    .reviComDesc(top.getReviComDesc())
                    .reviComDate(top.getReviComDate())
                    .user(UserDTO.builder()
                            .id(top.getUser().getId())
                            .name(top.getUser().getName())
                            .build())
                    .replies(repliesDTO)
                    .build();
            response.add(dto);
        }
        return ResponseEntity.ok(response);
    }


}

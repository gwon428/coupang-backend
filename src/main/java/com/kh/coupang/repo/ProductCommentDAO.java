package com.kh.coupang.repo;

import com.kh.coupang.domain.ProductComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductCommentDAO extends JpaRepository<ProductComment, Integer> {

     // 상품 1개에 따른 댓글 전체 조회
    // SELECT * FROM product_comment WHERE prod_code="{}";
    
    /* JpaRepository에서 PRIMARY KEY로만 연결
        - 전체 조회할 때 PRIMARY KEY가 아니기 때문에 기본으로 제공 X
    */
    
    // nativeQuery : JPA 방식 말고 내가 짠 쿼리문대로 가져오겠다
    @Query(value="SELECT * FROM product_comment WHERE prod_code = :code", nativeQuery = true)
    List<ProductComment> findByProdCode(@Param("code") int code);

}

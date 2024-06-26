package com.kh.coupang.service;

import com.kh.coupang.domain.ProductComment;
import com.kh.coupang.domain.QProductComment;
import com.kh.coupang.repo.ProductCommentDAO;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ProductCommentService {
    @Autowired
    private ProductCommentDAO dao;

    @Autowired
    private JPAQueryFactory queryFactory;

    private final QProductComment qProductComment = QProductComment.productComment;

    // 댓글 추가
    public ProductComment create(ProductComment vo){

        return dao.save(vo);
    }
/*
    // 상품 1개당 댓글 조회
    public List<ProductComment> findByProdCode(int prod_code){
        return dao.findByProdCode(prod_code);
    }
*/
    // query문 : SELECT * FROM prod_comment WHERE pro_com_parent=0 AND prod_code = 'code' ORDER BY pro_com_date DESC;
    // 상위 댓글만 조회
    public List<ProductComment> getTopLevelComments(int code){
        return queryFactory.selectFrom(qProductComment)
                .where(qProductComment.proComParent.eq(0))
                .where(qProductComment.prodCode.eq(code))
                .orderBy(qProductComment.proComDate.asc())
                .fetch();

    }

    // qeury문 : SELECT * FROM product_comment WHERE pro_com_parent='parent' AND prod_code='code' ORDER BY pro_com_date ASC
    // 하위 댓글만 조회
    public List<ProductComment> getRepliesComments(int parent, int code){
        return queryFactory.selectFrom(qProductComment)
                .where(qProductComment.proComParent.eq(parent))
                .where(qProductComment.prodCode.eq(code))
                .orderBy(qProductComment.proComDate.asc())
                .fetch();
    }
}

package com.kh.coupang.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @DynamicInsert
public class Product {

    @Id @Column(name="prod_code")
    // 추가와 동시에 바로 사용하려는 경우
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int prodCode;
    @Column(name="prod_name")
    private String prodName;
    @Column
    private int price;

    @ManyToOne @JoinColumn(name="cate_code")
    private Category category;
}

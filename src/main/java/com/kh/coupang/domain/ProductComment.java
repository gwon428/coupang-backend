package com.kh.coupang.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.util.Date;

@Entity @Data @NoArgsConstructor @AllArgsConstructor @DynamicInsert
public class ProductComment {
    @Id @Column(name="pro_com_code")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int proComCode;
    
    @Column(name="pro_com_desc")
    private String proComDesc;
    
    @Column(name="pro_com_date")
    private Date proComDate;
    

    @Column(name="pro_com_parent")
    private int proComParent;

    // 셀프 조인
    @JsonIgnore // 가공시키기 때문에 JSON에서 무시시키기
    @ManyToOne
    @JoinColumn(name="pro_com_parent", referencedColumnName = "pro_com_code", insertable = false, updatable = false)
    private ProductComment parent;

    @ManyToOne
    @JoinColumn(name="id")
    private Member user;
    
    // foreignkey 걸었다고 해서 무조건 객체 타입으로 가져올 필요는 X. 해당하는 foreignkey만 담는 변수만 있어도 괜찮음
    @Column(name="prod_code")
    private int prodCode;
    
}

package org.geekbang.time.commonmistakes.nullvalue.pojonull;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;


/**
 * @description 11-2 POJO 中属性的 null
 */
@Data
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String name;
    private String nickname;
    private Integer age;
    private Date createDate = new Date();
}

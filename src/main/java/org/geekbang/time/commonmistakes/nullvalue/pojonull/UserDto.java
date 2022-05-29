package org.geekbang.time.commonmistakes.nullvalue.pojonull;

import lombok.Data;

import java.util.Optional;

/**
 * @description  name 和 age 使用 Optional 来包装，以区分客户端不传数据还是故意传 null。
 * 如果不传值，那么 Optional 本身为 null，直接跳过 Entity 字段的更新即可，这样动态生成的 SQL (@DynamicUpdate  只更新修改后的字段)就不会包含这个列；
 * 如果传了值，那么进一步判断传的是不是 null。
 */
@Data
public class UserDto {
    private Long id;
    private Optional<String> name;
    private Optional<Integer> age;
}

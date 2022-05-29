package org.geekbang.time.commonmistakes.nullvalue.pojonull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("pojonull")
@RestController
public class POJONullController {

    @Autowired
    private UserEntityRepository userEntityRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public void test() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        UserDto result = objectMapper.readValue("{\"id\":\"1\", \"age\":30, \"name\":null}", UserDto.class);
        log.info("field name with null value dto:{} name:{}", result, result.getName().orElse("N/A"));
        // field name with null value dto:UserDto(id=1, name=Optional.empty, age=Optional[30]) name:N/A
        log.info("missing field name dto:{}", objectMapper.readValue("{\"id\":\"1\", \"age\":30}", UserDto.class));
        // missing field name dto:UserDto(id=1, name=null, age=Optional[30])
    }

    @PostMapping("wrong")
    public User wrong(@RequestBody User user) {
        user.setNickname(String.format("guest%s", user.getName()));
        return userRepository.save(user);
    }

    @PostMapping("right")
    public UserEntity right(@RequestBody UserDto user) {
        //对入参和ID 属性先判空
        if (user == null || user.getId() == null)
            throw new IllegalArgumentException("用户Id不能为空");

        //根据 id 从数据库中查询出实体后进行判空，
        UserEntity userEntity = userEntityRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        //一步判断传的name和age是不是 null
        //对于姓名，我们认为客户端传 null 是希望把姓名重置为空 在数据库中插入""即可
        if (user.getName() != null) {
            userEntity.setName(user.getName().orElse(""));
        }
        //对于昵称，因为数据库中姓名不可能为 null，Entity中的@Column(nullable = false)保证
        userEntity.setNickname("guest" + userEntity.getName());
        //对于年龄，认为如果客户端希望更新年龄就必须传一个有效的年龄，年龄不存在重 置操作
        if (user.getAge() != null) {
            userEntity.setAge(user.getAge().orElseThrow(() -> new IllegalArgumentException("年龄不能为空")));
        }
        return userEntityRepository.save(userEntity);
    }
}
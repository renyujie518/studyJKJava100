package org.geekbang.time.commonmistakes.transaction.transactionproxyfailed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

/**
 * @description 06-1  事务生效问题
 */
@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService self;

    @PostConstruct
    public void init() {
        log.info("this {} self {}", this.getClass().getName(), self.getClass().getName());
    }

    //私有方法  private 方法无法代理到  所以@Transactional不生效
    public int createUserWrong1(String name) {
        try {
            this.createUserPrivate(new UserEntity(name));
        } catch (Exception ex) {
            log.error("create user failed because {}", ex.getMessage());
        }
        return userRepository.findByName(name).size();
    }

    @Transactional
    private void createUserPrivate(UserEntity entity) {
        userRepository.save(entity);
        if (entity.getName().contains("test"))
            throw new RuntimeException("invalid username!");
    }




    //自调用 虽然改为了public
    //但this 指针代表对象自己，Spring 不可能注入 this，所以通过 this 访问方法必然不是代理。所以@Transactional还是不生效
    public int createUserWrong2(String name) {
        try {
            this.createUserPublic(new UserEntity(name));
        } catch (Exception ex) {
            log.error("create user failed because {}", ex.getMessage());
        }
        return userRepository.findByName(name).size();
    }

    //可以传播出异常
    @Transactional
    public void createUserPublic(UserEntity entity) {
        userRepository.save(entity);
        if (entity.getName().contains("test"))
            throw new RuntimeException("invalid username!");
    }


    //重新注入自己(但一般不会这么写)  self 是由 Spring 通 过 CGLIB 方式增强过的类  所以@Transactional生效
    public int createUserRight(String name) {
        try {
            self.createUserPublic(new UserEntity(name));
        } catch (Exception ex) {
            log.error("create user failed because {}", ex.getMessage());
        }
        return userRepository.findByName(name).size();
    }


    //不出异常  没有抛出异常 导致事务即 便生效也不一定能回滚。   所以@Transactional还是不生效
    @Transactional
    public int createUserWrong3(String name) {
        try {
            this.createUserPublic(new UserEntity(name));
        } catch (Exception ex) {
            log.error("create user failed because {}", ex.getMessage());
        }
        return userRepository.findByName(name).size();
    }

    public int getUserCount(String name) {
        return userRepository.findByName(name).size();
    }
}

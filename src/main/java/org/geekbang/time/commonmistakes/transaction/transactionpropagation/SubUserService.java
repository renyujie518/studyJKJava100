package org.geekbang.time.commonmistakes.transaction.transactionpropagation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
public class SubUserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void createSubUserWithExceptionWrong(UserEntity entity) {
        log.info("createSubUserWithExceptionWrong start");
        userRepository.save(entity);
        //抛出了一个运行时异常，错误原因是用户状态无效，所以子用户的注册肯定是失败的
        throw new RuntimeException("invalid status");
    }

    /**
     * @description  子逻辑在独立事务中运行
     * propagation = Propagation.REQUIRES_NEW 设置 REQUIRES_NEW 方式的事务传播策略
     * 执 行到这个方法时需要开启新的事务，并挂起当前事务
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createSubUserWithExceptionRight(UserEntity entity) {
        log.info("createSubUserWithExceptionRight start");
        userRepository.save(entity);
        throw new RuntimeException("invalid status");
    }
}

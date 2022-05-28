package org.geekbang.time.commonmistakes.transaction.transactionrollbackfailed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @description 06-2 事务回滚问题
 * 1.@Transactional修饰的方法是有异常被捕捉到
 * 2.出现 RuntimeException（非受检异常）或 Error 的时候，Spring 才会回滚事务。
 */
@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;

    /**
     * @description
     * 会抛出一个 RuntimeException，但由于方法内 catch 了所有异常Exception，异常无法从方法传播出去，事务自然无法回滚
     */
    @Transactional
    public void createUserWrong1(String name) {
        try {
            userRepository.save(new UserEntity(name));
            throw new RuntimeException("error");
        } catch (Exception ex) {
            log.error("create user failed", ex);
        }
    }

    @Transactional
    public void createUserWrong2(String name) throws IOException {
        userRepository.save(new UserEntity(name));
        otherTask();
    }

    /**
     * @description 模拟文件读取  但捕获抛出IO异常  createUserWrong2抛出的是受检异常（非RuntimeException）
     */
    private void otherTask() throws IOException {
        Files.readAllLines(Paths.get("file-that-not-exist"));
    }

    public int getUserCount(String name) {
        return userRepository.findByName(name).size();
    }


    @Transactional
    public void createUserRight1(String name) {
        try {
            userRepository.save(new UserEntity(name));
            throw new RuntimeException("error");
        } catch (Exception ex) {
            log.error("create user failed", ex);
            //一旦出现了异常，手动设置让当前事务处于回滚状态
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        log.info("result {} ", userRepository.findByName(name).size());//为什么这里是1你能想明白吗？
    }

    /**
     * @description 在注解中声明，期望遇到所有的 Exception 都回滚事务（来突破默认不回滚受检异常的限制）这个是最常用的做法
     */
    //DefaultTransactionAttribute
    @Transactional(rollbackFor = Exception.class)
    public void createUserRight2(String name) throws IOException {
        userRepository.save(new UserEntity(name));
        otherTask();
    }

}

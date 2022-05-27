package org.geekbang.time.commonmistakes.connectionpool.datasource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;
/**
 * @description 04-3
 * 模拟下压力增大导致数据库连接池打满的情况
 * 最终落实到数据库连接池最大连接参数到 50
 */
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User register() {
        User user = new User();
        user.setName("new-user-" + System.currentTimeMillis());
        userRepository.save(user);
        try {
            //一个数据库事务对应一个 TCP 连接，所以 500 多毫秒的时间都会占用数 据库连接
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return user;
    }

}

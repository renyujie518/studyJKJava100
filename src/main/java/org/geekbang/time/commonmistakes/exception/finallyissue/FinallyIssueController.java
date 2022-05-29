package org.geekbang.time.commonmistakes.exception.finallyissue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * @description 12 -2 finally异常
 */
@RestController
@Slf4j
@RequestMapping("finallyissue")
public class FinallyIssueController {

    @GetMapping("useresourcewrong")
    public void useresourcewrong() throws Exception {
        TestResource testResource = new TestResource();
        try {
            testResource.read();
        } finally {
            testResource.close();
        }
    }

    @GetMapping("useresourceright")
    public void useresourceright() throws Exception {
        try (TestResource testResource = new TestResource()) {
            testResource.read();
        }
    }

    /**
     * @description 因为一个方法无法出现两个异常  所以fianlly中的异常会覆盖try中的异常
     */
    @GetMapping("wrong")
    public void wrong() {
        try {
            log.info("try");
            throw new RuntimeException("try");
        } finally {
            log.info("finally");
            throw new RuntimeException("finally");
        }
    }

    /**
     * @description 为防止覆盖  finally 代码块自己负责异常捕获和处理
     */
    @GetMapping("right")
    public void right() {
        try {
            log.info("try");
            throw new RuntimeException("try");
        } finally {
            log.info("finally");
            try {
                throw new RuntimeException("finally");
            } catch (Exception ex) {
                log.error("finally", ex);
            }
        }
    }

    /**
     * @description 或者可以把 try 中的异常作为主异常抛出，使用 addSuppressed 方法把 finally 中的异常 附加到主异常上
     */
    @GetMapping("right2")
    public void right2() throws Exception {
        Exception e = null;
        try {
            log.info("try");
            throw new RuntimeException("try");
        } catch (Exception ex) {
            e = ex;
        } finally {
            log.info("finally");
            try {
                throw new RuntimeException("finally");
            } catch (Exception ex) {
                if (e != null) {
                    e.addSuppressed(ex);
                } else {
                    e = ex;
                }
            }
        }
        throw e;
    }
}

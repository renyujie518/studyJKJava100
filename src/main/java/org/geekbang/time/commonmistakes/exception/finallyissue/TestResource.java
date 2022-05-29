package org.geekbang.time.commonmistakes.exception.finallyissue;
/**
 * @description 12-2 try-with-resources 语句的做法，
 * 对于实现了 AutoCloseable 接口的资源，建 议使用 try-with-resources 来释放资源，
 */
public class TestResource implements AutoCloseable {

    public void read() throws Exception {
        throw new Exception("read error");
    }

    @Override
    public void close() throws Exception {
        throw new Exception("close error");
    }
}

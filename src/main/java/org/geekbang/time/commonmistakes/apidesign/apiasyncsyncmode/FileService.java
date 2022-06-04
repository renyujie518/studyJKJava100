package org.geekbang.time.commonmistakes.apidesign.apiasyncsyncmode;

import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FileService {

    private ExecutorService threadPool = Executors.newFixedThreadPool(2);
    private AtomicInteger atomicInteger = new AtomicInteger(0);
    //用map缓存暂存上传操作的结果，生产代码需要考虑数据持久化（比如redis）
    private ConcurrentHashMap<String, SyncQueryUploadTaskResponse> downloadUrl = new ConcurrentHashMap<>();


    /**
     * @description 模拟上传原图
     * 传入二进制文件，传出下载地址
     */
    private String uploadFile(byte[] data) {
        try {
            TimeUnit.MILLISECONDS.sleep(500 + ThreadLocalRandom.current().nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "http://www.demo.com/download/" + UUID.randomUUID().toString();
    }

    /**
     * @description 模拟上传缩略图
     * 传入二进制文件，传出下载地址
     */
    private String uploadThumbnailFile(byte[] data) {
        try {
            TimeUnit.MILLISECONDS.sleep(1500 + ThreadLocalRandom.current().nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "http://www.demo.com/download/" + UUID.randomUUID().toString();
    }

    /**
     * @description 上传接口在内部需要进行两步操作，首先上传原图，然后压缩后上传缩略图。
     * 一旦遇到超时，接口就不能返回完整的数据
     */
    public UploadResponse upload(UploadRequest request) {
        UploadResponse response = new UploadResponse();
        //异步处理
        Future<String> uploadFile = threadPool.submit(() -> uploadFile(request.getFile()));
        Future<String> uploadThumbnailFile = threadPool.submit(() -> uploadThumbnailFile(request.getFile()));
        //等待上传原始文件任务完成，最多等待1秒
        try {
            response.setDownloadUrl(uploadFile.get(1, TimeUnit.SECONDS));
        } catch (Exception e) {
            e.printStackTrace();
        }
        ////等待上传缩略图任务完成，最多等待1秒
        try {
            response.setThumbnailDownloadUrl(uploadThumbnailFile.get(1, TimeUnit.SECONDS));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * @description 正确的同步上传接口（其实就是串行执行）  把超时的选择留给客户端
     */
    public SyncUploadResponse syncUpload(SyncUploadRequest request) {
        SyncUploadResponse response = new SyncUploadResponse();
        response.setDownloadUrl(uploadFile(request.getFile()));
        response.setThumbnailDownloadUrl(uploadThumbnailFile(request.getFile()));
        return response;
    }

    /**
     * @description 异步上传接口在出参上有点区别，不再返 回文件 URL，而是返回一个任务 ID：
     * 上传接口响应很快，客户端需要之后再拿着任务 ID 调用任务查询接口查询上传的 文件 URL。
     * 完成后把结果写入一个 HashMap（downloadUrl），任务查询接口通过查询这个 HashMap 来获得文件 的 URL
     */
    public AsyncUploadResponse asyncUpload(AsyncUploadRequest request) {
        AsyncUploadResponse response = new AsyncUploadResponse();
        String taskId = "upload" + atomicInteger.incrementAndGet();
        //异步上传操作只返回任务ID
        response.setTaskId(taskId);
        threadPool.execute(() -> {
            String url = uploadFile(request.getFile());
            //如果ConcurrentHashMap不包含Key，则初始化一个SyncQueryUploadTaskResponse 相当于把异步返回结果的DTO当做value
            downloadUrl.computeIfAbsent(taskId, id -> new SyncQueryUploadTaskResponse(id)).setDownloadUrl(url);
        });
        threadPool.execute(() -> {
            String url = uploadThumbnailFile(request.getFile());
            downloadUrl.computeIfAbsent(taskId, id -> new SyncQueryUploadTaskResponse(id)).setThumbnailDownloadUrl(url);
        });
        return response;
    }

    public SyncQueryUploadTaskResponse syncQueryUploadTask(SyncQueryUploadTaskRequest request) {
        //依据任务ID获得暂存的异步返回结果的DTO
        SyncQueryUploadTaskResponse response = new SyncQueryUploadTaskResponse(request.getTaskId());
        //分装成视图层的异步返回结果DTO
        response.setDownloadUrl(downloadUrl.getOrDefault(request.getTaskId(), response).getDownloadUrl());
        response.setThumbnailDownloadUrl(downloadUrl.getOrDefault(request.getTaskId(), response).getThumbnailDownloadUrl());
        return response;
    }
}

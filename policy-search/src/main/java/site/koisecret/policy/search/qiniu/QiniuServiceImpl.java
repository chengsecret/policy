package site.koisecret.policy.search.qiniu;

import cn.hutool.json.JSONUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.koisecret.commons.Response.Result;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/**
 * @author by chengsecret
 * @date 2023/4/8.
 */
@Service
@Slf4j
public class QiniuServiceImpl {

    @Autowired
    private QiniuProperties qiniuProperties;

    public Result uploadImage(MultipartFile file) throws IOException {
        try {
            int dotPos = Objects.requireNonNull(file.getOriginalFilename()).lastIndexOf(".");
            if (dotPos < 0) {
                return Result.FAIL("请添加文件后缀");
            }
            String fileExt = file.getOriginalFilename().substring(dotPos + 1).toLowerCase();
            // 判断是否是合法的文件后缀
            if (!FileUtil.isFileAllowed(fileExt)) {
                return Result.FAIL("文件后缀不符合要求");
            }

            String fileName = UUID.randomUUID().toString().replaceAll("-", "") + "." + fileExt;

            // 密钥配置
            Auth auth = Auth.create(qiniuProperties.getAccessKey(), qiniuProperties.getSecretKey());
            // 构造一个带指定Zone对象的配置类,不同的七云牛存储区域调用不同的zone
            Configuration cfg = new Configuration(Region.region0());
            UploadManager uploadManager = new UploadManager(cfg);
            String token = auth.uploadToken(qiniuProperties.getBucket());
            // 调用put方法上传
            Response res = uploadManager.put(file.getBytes(), fileName, token);
            // 打印返回的信息
            if (res.isOK() && res.isJson()) {
                // 返回这张存储照片的地址
                String url = qiniuProperties.getPrefix() + JSONUtil.parseObj(res.bodyString()).get("key");
                return Result.SUCCESS().set("url",url);

            } else {
                log.error("七牛异常:" + res.bodyString());
                return Result.FAIL( res.bodyString());
            }
        } catch (QiniuException e) {
            // 请求失败时打印的异常的信息
            log.error("七牛异常:" + e.getMessage());
            return Result.FAIL( e.getMessage());
        }
    }

}

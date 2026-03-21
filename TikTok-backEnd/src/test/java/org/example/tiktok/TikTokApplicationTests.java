package org.example.tiktok;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import org.example.tiktok.dto.EmailCodeDTO;
import org.example.tiktok.dto.PageBean;
import org.example.tiktok.entity.Result;
import org.example.tiktok.entity.Video.Video;
import org.example.tiktok.mapper.VideoMapper;
import org.example.tiktok.service.IndexService;
import org.example.tiktok.service.LoginService;
import org.example.tiktok.utils.AliOSSUtil;
import org.example.tiktok.utils.CacheClient;
import org.example.tiktok.utils.HotRank;
import org.example.tiktok.utils.SnowflakeIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootTest
class TikTokApplicationTests {

    @Resource
    LoginService userService;

    @Resource
    IndexService indexService;

    @Resource
    AliOSSUtil aliOSSUtil;

    @Resource
    SnowflakeIdWorker snowflakeIdWorker;

    @Resource
    HotRank hotRank;

    @Resource
    VideoMapper videoMapper;

    @Resource
    CacheClient cacheClient;



    @Test
    void testMail() {
        EmailCodeDTO emailCodeDTO = new EmailCodeDTO("661234","2131","1271892479@qq.com");
        userService.sendMail(emailCodeDTO);
    }

    @Value("${ALIYUN_OSS_ACCESS_KEY_ID}")
    private String AccessKeyId;

    @Value("${ALIYUN_OSS_ACCESS_KEY_SECRET}")
    private String AccessKeySecret;

    @Test
    void initDataBaseFromAliOss() {
        // === 1. OSS 客户端配置 ===
        // 通过控制台获取你的 AccessKeyId/AccessKeySecret
        String accessKeyId = AccessKeyId;
        String accessKeySecret = AccessKeySecret;
        // OSS Endpoint（去掉前缀 protocol，仅保留域名）
        String endpoint       = "oss-cn-shanghai.aliyuncs.com";
        // Bucket 名称，根据你的 URL 可知：bucket 为 save-avatar
        String bucketName     = "save-avatar";
        // 该前缀指向你想列举的目录：/video/JuClubFootball/ 下的所有对象
        String prefix = "video/song/";

        // 初始化 OSSClient
        OSS ossClient = new OSSClientBuilder().build("https://" + endpoint, accessKeyId, accessKeySecret);

        // === 2. 列出所有目标视频对象 Key ===
        List<String> allVideoKeys = new ArrayList<>();
        String nextMarker = null;
        int maxKeys = 1000;

        do {
            ListObjectsRequest listReq = new ListObjectsRequest(bucketName)
                    .withPrefix(prefix)
                    .withMarker(nextMarker)
                    .withMaxKeys(maxKeys);
            ObjectListing listing = ossClient.listObjects(listReq);

            for (OSSObjectSummary summary : listing.getObjectSummaries()) {
                String key = summary.getKey();
                // 根据需要，只保留常见视频后缀（例如 .mp4、.avi、.mkv）。若目录中只有视频，可省略后缀判断。
                if (key.endsWith(".mp4") || key.endsWith(".avi") || key.endsWith(".mkv")) {
                    allVideoKeys.add(key);
                }
            }
            nextMarker = listing.getNextMarker();
        } while (nextMarker != null && !nextMarker.isEmpty());

        System.out.println("找到视频文件数量：" + allVideoKeys.size());

        // === 3. 为每个 ObjectKey 生成可访问的 URL ===
        // 假设你的 Bucket 已设置为“公众读”，我们直接拼接公开 URL：
        //    https://{bucketName}.{endpoint}/{objectKey}
        List<String> videoUrls = new ArrayList<>(allVideoKeys.size());
        String urlPattern = "https://%s.%s/%s";
        for (String key : allVideoKeys) {
            String url = String.format(urlPattern, bucketName, endpoint, key);
            videoUrls.add(url);
        }
        System.out.println(videoUrls);
        for (int i = 0; i < allVideoKeys.size(); i++) {
            String objectKey = allVideoKeys.get(i);
            String videoUrl  = videoUrls.get(i);

            // 从 objectKey 中提取文件名作为 title，比如 "video/JuClubFootball/match1.mp4" -> "match1.mp4"
            String fileName = objectKey.substring(objectKey.lastIndexOf('/') + 1);

            // 实例化 Video 对象并设置属性
            Video video = new Video();
            video.setTitle(fileName);
            video.setDescription("");   // 暂时留空，可根据需求填写
            // 从文件名提取后缀作为 type，比如 "match1.mp4" -> "mp4"
            video.setType("song");
            video.setSource(videoUrl);        // 将 URL 存到 source 字段
            video.setImgSource("");           // 如果有缩略图地址可填写，否则留空
            video.setVideoTypeId(1L);         // 示例：全部设置为 1，你也可以根据目录或后缀来决定类别
            video.setPublisherId(10L);         // 示例：发布者 ID，若每个视频同一个发布者，可设固定值

            // ★ 在这里随机生成 0～10000 之间的数值 ★
            long randomLikes      = ThreadLocalRandom.current().nextLong(0, 10001);
            long randomViews      = ThreadLocalRandom.current().nextLong(0, 10001);
            long randomFavourites = ThreadLocalRandom.current().nextLong(0, 10001);
            long randomShares     = ThreadLocalRandom.current().nextLong(0, 10001);



            video.setLikes(randomLikes);
            video.setViews(randomViews);
            video.setFavourites(randomFavourites);
            video.setShares(randomShares);
            video.setCreateTime(LocalDateTime.now());
            video.setUpdateTime(LocalDateTime.now());
            video.setComments(0L);            // 如果你在表里配置了 comments 字段，可留空

            // 调用 MyBatis Mapper 将这条记录插入到数据库
            videoMapper.addVideo(video);
        }

        // 关闭 OSSClient
        ossClient.shutdown();
        System.out.println("所有视频已插入数据库。");

    }

    //pass
    @Test
    public void testUploadFile() {
        try {
            // 本地图片路径
            String localFilePath = "E:\\png\\Logo.png";
            // 生成上传后的文件名（你可以自定义，也可以加时间戳防止重复）
            String objectName = "Thisisatest/Logo.png";

            // 读取文件为 InputStream
            InputStream inputStream = new FileInputStream(localFilePath);

            // 调用上传方法
            String url = aliOSSUtil.uploadFile(objectName, inputStream);
            System.out.println("this test for git is work");
            System.out.println("上传成功！访问地址：" + url);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testSnow() {
        System.out.println(snowflakeIdWorker.nextId());
        System.out.println(snowflakeIdWorker.nextId());
    }

    @Test
    public void getHotVideo() throws JsonProcessingException {
        PageBean<Video> recentHotVideo = hotRank.getRecentHotVideo(1,10);
        System.out.println(recentHotVideo);
        System.out.println("分割线-------------");
        PageBean<Video> recentHotVideo2 = hotRank.getRecentHotVideo(2,10);
        System.out.println(recentHotVideo2);
    }

    @Test
    public void testGetAllTypes() {
        Result allVideoTypes = indexService.getAllVideoTypes();

        System.out.println(allVideoTypes);
    }

    @Test
    public void testGetVideosByTypeId() {
        Result videosByTypeId = indexService.getVideosByTypeId(1L, 1, 10);
        System.out.println(videosByTypeId.getData());
    }



    @Test
    public void testHotRank() throws JsonProcessingException {
        hotRank.calculateDailyHotRank();
        System.out.println(indexService.getHotRank());
    }




}

package org.example.tiktok.mq;

import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.tiktok.config.RabbitMQConfig;
import org.example.tiktok.dto.UploadTaskDTO;
import org.example.tiktok.utils.AliOSSUtil;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
//for future use, currently not used
public class FileUploadConsumer {
    @Resource
    AliOSSUtil aliOSSUtil;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @RabbitListener(queues = RabbitMQConfig.UPLOAD_QUEUE)
    public void handleUpload(UploadTaskDTO task, Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        String taskId = task.getTaskId();
        try {
            // 执行实际上传
            String url = aliOSSUtil.uploadFile(
                    task.getFilename(),
                    new ByteArrayInputStream(task.getFileBytes()),
                    task.getType()
            );
            // 把结果存入Redis，前端轮询获取
            stringRedisTemplate.opsForValue().set(
                    "upload:task:" + taskId, url, 10, TimeUnit.MINUTES
            );
            log.info("文件上传成功: taskId={}, url={}", taskId, url);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("文件上传失败: taskId={}, error={}", taskId, e.getMessage());
            // 把失败状态存入Redis
            try {
                stringRedisTemplate.opsForValue().set(
                        "upload:task:" + taskId, "FAILED:" + e.getMessage(), 10, TimeUnit.MINUTES
                );
                channel.basicNack(deliveryTag, false, false); // 不重试，避免重复上传
            } catch (IOException ex) {
                log.error("消息处理失败", ex);
            }
        }
    }
}

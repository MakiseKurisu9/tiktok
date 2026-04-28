package org.example.tiktok.mq;

import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.tiktok.config.RabbitMQConfig;
import org.example.tiktok.dto.LikeMessage;
import org.example.tiktok.mapper.VideoMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class LikeConsumer {

    @Resource
    VideoMapper videoMapper;

    @RabbitListener(queues = RabbitMQConfig.LIKE_QUEUE)
    public void handleLike(LikeMessage msg, Channel channel,
                           @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            if (msg.getDelta() == 1) {
                // 点赞：更新DB
                videoMapper.starVideo(msg.getVideoId());
                videoMapper.videoLike(msg.getVideoId(), msg.getUserId());
            } else {
                // 取消点赞：更新DB
                videoMapper.decreaseStarVideo(msg.getVideoId());
                videoMapper.videoNotLike(msg.getVideoId(), msg.getUserId());
            }
            log.info("点赞消息处理成功: videoId={}, delta={}", msg.getVideoId(), msg.getDelta());
            // 告诉RabbitMQ这条消息处理成功，可以删除了
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("点赞消息处理失败: {}", e.getMessage());
            try {
                // 处理失败，消息重新入队重试
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                log.error("消息重入队失败", ex);
            }
        }
    }
}
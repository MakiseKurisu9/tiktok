package org.example.tiktok.mq;

import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.tiktok.config.RabbitMQConfig;
import org.example.tiktok.entity.Video.Video;
import org.example.tiktok.entity.Video.VideoDocument;
import org.example.tiktok.mapper.IndexMapper;
import org.example.tiktok.repository.VideoEsRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class VideoSyncConsumer {

    @Resource
    IndexMapper indexMapper;

    @Resource
    VideoEsRepository videoEsRepository;

    @RabbitListener(queues = RabbitMQConfig.VIDEO_SYNC_QUEUE)
    public void handleVideoSync(Long videoId, Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            Video video = indexMapper.getVideoById(videoId);
            if (video != null) {
                VideoDocument doc = convertToDocument(video);
                videoEsRepository.save(doc);
                log.info("视频同步ES成功: videoId={}", videoId);
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("视频同步ES失败: videoId={}, error={}", videoId, e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                log.error("消息重入队失败", ex);
            }
        }
    }

    private VideoDocument convertToDocument(Video video) {
        VideoDocument doc = new VideoDocument();
        doc.setId(video.getId());
        doc.setTitle(video.getTitle());
        doc.setDescription(video.getDescription());
        doc.setType(video.getType());
        doc.setSource(video.getSource());
        doc.setImgSource(video.getImgSource());
        doc.setPublisherId(video.getPublisherId());
        doc.setPublisherName(video.getPublisherName());
        doc.setLikes(video.getLikes());
        doc.setViews(video.getViews());
        doc.setFavourites(video.getFavourites());
        doc.setCreateTime(video.getCreateTime());
        return doc;
    }
}
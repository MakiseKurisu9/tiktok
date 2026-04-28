package org.example.tiktok.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RabbitMQConfig {

    // 点赞相关常量
    public static final String LIKE_QUEUE = "video.like.queue";
    public static final String LIKE_EXCHANGE = "video.like.exchange";
    public static final String LIKE_ROUTING_KEY = "video.like";

    // 视频同步ES相关常量
    public static final String VIDEO_SYNC_QUEUE = "video.sync.queue";
    public static final String VIDEO_SYNC_EXCHANGE = "video.sync.exchange";
    public static final String VIDEO_SYNC_ROUTING_KEY = "video.sync";

    public static final String UPLOAD_QUEUE = "file.upload.queue";
    public static final String UPLOAD_EXCHANGE = "file.upload.exchange";
    public static final String UPLOAD_ROUTING_KEY = "file.upload";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public DirectExchange likeExchange() {
        return new DirectExchange(LIKE_EXCHANGE);
    }

    @Bean
    public Queue likeQueue() {
        return QueueBuilder.durable(LIKE_QUEUE).build();
    }

    @Bean
    public Binding likeBinding() {
        return BindingBuilder.bind(likeQueue()).to(likeExchange()).with(LIKE_ROUTING_KEY);
    }

    @Bean
    public DirectExchange videoSyncExchange() {
        return new DirectExchange(VIDEO_SYNC_EXCHANGE);
    }

    @Bean
    public Queue videoSyncQueue() {
        return QueueBuilder.durable(VIDEO_SYNC_QUEUE).build();
    }

    @Bean
    public Binding videoSyncBinding() {
        return BindingBuilder.bind(videoSyncQueue()).to(videoSyncExchange()).with(VIDEO_SYNC_ROUTING_KEY);
    }

    @Bean
    public DirectExchange uploadExchange() {
        return new DirectExchange(UPLOAD_EXCHANGE);
    }

    @Bean
    public Queue uploadQueue() {
        return QueueBuilder.durable(UPLOAD_QUEUE).build();
    }

    @Bean
    public Binding uploadBinding() {
        return BindingBuilder.bind(uploadQueue()).to(uploadExchange()).with(UPLOAD_ROUTING_KEY);
    }
}




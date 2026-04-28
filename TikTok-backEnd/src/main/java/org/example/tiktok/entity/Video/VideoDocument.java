package org.example.tiktok.entity.Video;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Data
@Document(indexName = "video")
public class VideoDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String description;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Keyword)
    private String source;

    @Field(name ="img_source",type = FieldType.Keyword)
    private String imgSource;

    @Field(name="video_type_id",type = FieldType.Long)
    private Long videoTypeId;

    @Field(name="publisher_id",type = FieldType.Long)
    private Long publisherId;

    @Field(name = "publisher_name",type = FieldType.Keyword)
    private String publisherName;

    @Field(type = FieldType.Long)
    private Long likes;

    @Field(type = FieldType.Long)
    private Long views;

    @Field(type = FieldType.Long)
    private Long favourites;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;
}
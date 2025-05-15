package org.example.tiktok.entity.FileProtect;

import lombok.Data;

@Data
//保护存储视频的域名，前端请求 先获取id，通过id再获得key
public class FileProtect {
    private Long id;

    private String key;
    //文件信息，使得不用通过第三方OSS查看文件数量类型等
    private String fileType;

    private Long fileSize;
    //video or png
    private String mediaType;
    //视频时长
    private Integer duration;

}

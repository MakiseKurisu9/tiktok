package org.example.tiktok.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadTaskDTO implements Serializable {
    private String taskId;
    private String filename;
    private byte[] fileBytes;
    private String type;


}

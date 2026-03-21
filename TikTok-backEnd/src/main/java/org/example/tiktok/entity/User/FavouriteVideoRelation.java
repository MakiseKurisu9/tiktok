package org.example.tiktok.entity.User;

import lombok.Data;

@Data
public class FavouriteVideoRelation {
    private Long id;
    private Long fid;
    private Long vid;
}

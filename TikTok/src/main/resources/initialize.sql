-- Create database tables based on Java entity classes
-- Using UTF-8MB4 encoding
-- No foreign key constraints for testing purposes

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS tiktok CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE tiktok;

-- FileProtect table
CREATE TABLE IF NOT EXISTS file_protect (
                                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                            `key` VARCHAR(255) NOT NULL,
                                            file_type VARCHAR(100),
                                            file_size BIGINT,
                                            media_type VARCHAR(50),
                                            duration INTEGER
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- VideoType table
CREATE TABLE IF NOT EXISTS video_type (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          name VARCHAR(100) NOT NULL,
                                          tag VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User table
CREATE TABLE IF NOT EXISTS user (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    nickname VARCHAR(100),
                                    email VARCHAR(255) NOT NULL,
                                    password VARCHAR(255) NOT NULL,
                                    user_description TEXT,
                                    avatar_source VARCHAR(255),
                                    create_time DATETIME NOT NULL,
                                    update_time DATETIME NOT NULL,
                                    sex VARCHAR(5),
                                    follow BIGINT DEFAULT 0,
                                    followers BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Video table
CREATE TABLE IF NOT EXISTS video (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     title VARCHAR(255) NOT NULL,
                                     description TEXT,
                                     type VARCHAR(100),
                                     source VARCHAR(30),
                                     img_source VARCHAR(30),
                                     video_type_id BIGINT,
                                     publisher_id BIGINT NOT NULL,
                                     publisher_name VARCHAR(20) NOT NULL,
                                     likes BIGINT DEFAULT 0,
                                     views BIGINT DEFAULT 0,
                                     favourites BIGINT DEFAULT 0,
                                     shares BIGINT DEFAULT 0,
                                     create_time DATETIME NOT NULL,
                                     update_time DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS comment (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       video_id BIGINT NOT NULL,
                                       from_user_id BIGINT NOT NULL,
                                       to_user_id BIGINT,
                                       content TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                       parent_id BIGINT,
                                       root_id BIGINT,
                                       likes_count INT DEFAULT 0,
                                       child_count INT DEFAULT 0,
                                       create_time DATETIME NOT NULL,
                                       update_time DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Favourite table
CREATE TABLE IF NOT EXISTS favourite (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         name VARCHAR(100) NOT NULL,
                                         description TEXT,
                                         video_count INTEGER DEFAULT 0,
                                         create_user_id BIGINT NOT NULL,
                                         create_time DATETIME NOT NULL,
                                         update_time DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS favourite_video_relation (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          fid BIGINT NOT NULL,
                                          vid BIGINT NOT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- Follow table
CREATE TABLE IF NOT EXISTS follow (
                                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      follow_id BIGINT NOT NULL,
                                      follower_id BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- SubscribeVideoType table
CREATE TABLE IF NOT EXISTS subscribe_video_type (
                                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                    user_id BIGINT NOT NULL,
                                                    video_type_id BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- UserFavouriteRelation table
CREATE TABLE IF NOT EXISTS user_favourite_relation (
                                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                       create_user_id BIGINT NOT NULL,
                                                       favourite_id BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;




-- VideoLike table
CREATE TABLE IF NOT EXISTS video_like (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          video_id BIGINT NOT NULL,
                                          like_id BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- VideoShare table
CREATE TABLE IF NOT EXISTS video_share (
                                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                           share_user_id BIGINT,
                                           ip VARCHAR(50),
                                           video_id BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- VideoTypeRelation table
CREATE TABLE IF NOT EXISTS video_type_relation (
                                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                   video_id BIGINT NOT NULL,
                                                   video_type_id BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for better performance
CREATE INDEX idx_video_publisher_id ON video(publisher_id);
CREATE INDEX idx_favourite_create_user_id ON favourite(create_user_id);
CREATE INDEX idx_follow_follow_id ON follow(follow_id);
CREATE INDEX idx_follow_follower_id ON follow(follower_id);
CREATE INDEX idx_subscribe_user_id ON subscribe_video_type(user_id);
CREATE INDEX idx_video_like_video_id ON video_like(video_id);
CREATE INDEX idx_video_share_video_id ON video_share(video_id);
CREATE INDEX idx_video_type_relation_video_id ON video_type_relation(video_id);
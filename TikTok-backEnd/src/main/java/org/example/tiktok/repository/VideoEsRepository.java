package org.example.tiktok.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface VideoEsRepository extends ElasticsearchRepository<org.example.tiktok.entity.Video.VideoDocument, Long> {
}

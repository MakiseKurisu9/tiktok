package org.example.tiktok.service;

import org.example.tiktok.dto.FavouriteDTO;
import org.example.tiktok.entity.Result;

public interface CustomerService {
    Result getCustomerFavourite();

    Result getFavouriteById(Long favouriteId);

    Result addOrUpdateFavourite(FavouriteDTO favouriteDTO);

    Result delFavourite(Long favouriteId);

    Result subscribeVideoTypes(String types);

    Result getSubscribeByUserId();

}

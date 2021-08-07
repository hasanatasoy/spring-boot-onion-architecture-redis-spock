package com.trendyol.basket.services.impl;

import com.trendyol.basket.entity.Basket;
import com.trendyol.basket.entity.ProductInfo;
import com.trendyol.basket.exception.BasketNotFoundException;
import com.trendyol.basket.repository.BasketRepository;
import com.trendyol.basket.services.BasketService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class BasketServiceImpl implements BasketService {

    private final BasketRepository basketRepository;

    public BasketServiceImpl(
            BasketRepository basketRepository){
        this.basketRepository = basketRepository;
    }

    @Override
    public Basket add(long customerId, long productId,
                       String imageUrl, String title, int quantity, BigDecimal price, BigDecimal oldPrice) {
        var optionalBasket = basketRepository.findByCustomerId(customerId);
        var productInfo = new ProductInfo(productId, imageUrl, title, quantity, price, oldPrice);
        Basket basket;
        if(optionalBasket.isEmpty()){
            basket = new Basket(customerId, productInfo);
        }
        else{
            basket = optionalBasket.get();
            var optProductInfo = basket.getProducts().stream().filter(pi -> pi.getId() == productId).findFirst();
            if(optProductInfo.isEmpty()){
                basket.addItemToBasket(productInfo);
            }
            else{
                var existProductInfo = optProductInfo.get();
                basket.setProductQuantity(existProductInfo.getId(), existProductInfo.getQuantity() + quantity);
            }
        }
        basketRepository.save(basket);
        return basket;
    }

    @Override
    public Basket update(long customerId, long productId, int quantity) {
        var optionalBasket = basketRepository.findByCustomerId(customerId);
        if(optionalBasket.isEmpty())
            throw new BasketNotFoundException();
        var basket = optionalBasket.get();
        basket.setProductQuantity(productId, quantity);
        basketRepository.save(basket);
        return basket;
    }

    @Override
    public Basket get(long customerId) {
        var basket = basketRepository.findByCustomerId(customerId)
                .orElseThrow(BasketNotFoundException::new);
        return basket;
    }

    @Override
    public void addCampaignToBasket(long customerId, String campaignDisplayName, BigDecimal campaignPrice) {
        var optionalBasket = basketRepository.findByCustomerId(customerId);
        if(optionalBasket.isEmpty())
            throw new BasketNotFoundException();
        var basket = optionalBasket.get();
        basket.getBasketInfo().updateGrandTotalWithCampaign(campaignDisplayName, campaignPrice);
        basketRepository.save(basket);
    }

    @Override
    public void clearBasketCampaigns(long customerId) {
        var optionalBasket = basketRepository.findByCustomerId(customerId);
        if(optionalBasket.isEmpty())
            throw new BasketNotFoundException();
        var basket = optionalBasket.get();
        basket.getBasketInfo().clearCampaigns();
        basketRepository.save(basket);
    }

    @Override
    public List<Basket> getByProductId(long productId) {
        var optionalBaskets = basketRepository.findByProductId(productId);
        List<Basket> baskets;
        if(optionalBaskets.isEmpty()){
            baskets = new ArrayList<>();
        }
        else{
            baskets = optionalBaskets.get();
        }
        return baskets;
    }
}
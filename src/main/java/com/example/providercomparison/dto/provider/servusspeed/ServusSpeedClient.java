package com.example.providercomparison.dto.provider.servusspeed;

import com.example.providercomparison.dto.provider.servusspeed.model.DetailedResponseData;
import com.example.providercomparison.dto.ui.SearchCriteria;

import java.util.List;

/**
 * Client interface for ServusSpeed API
 */
public interface ServusSpeedClient {
    /**
     * Fetches available product IDs using user search criteria
     */
    List<String> getAvailableProducts(SearchCriteria criteria);

    /**
     * Fetches detailed product data for a given product ID
     */
    DetailedResponseData getProductDetails(String productId, SearchCriteria criteria);
}

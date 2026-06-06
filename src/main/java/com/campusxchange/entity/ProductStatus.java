package com.campusxchange.entity;

/**
 * Product Status Enum - Status of product listing
 */
public enum ProductStatus {
    AVAILABLE,      // Product is available for purchase
    SOLD,          // Product has been sold
    PENDING,       // Listing is pending approval
    REJECTED,      // Listing was rejected
    DELETED,       // Listing was deleted by user
    ARCHIVED       // Listing was archived
}

# CampusXchange API Documentation

**Version:** 1.0.0  
**Base URL:** `http://localhost:8080/api`  
**Swagger UI:** `http://localhost:8080/swagger-ui.html`  
**OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## 🔐 Authentication

All protected endpoints require a **Bearer Token** in the `Authorization` header:

```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Token Expiration:** 24 hours  
**Refresh Token Expiration:** 7 days

---

## 📋 Table of Contents

1. [Authentication Endpoints](#authentication-endpoints)
2. [Product Endpoints](#product-endpoints)
3. [User Endpoints](#user-endpoints)
4. [Review Endpoints](#review-endpoints)
5. [Message Endpoints](#message-endpoints)
6. [Error Handling](#error-handling)
7. [Status Codes](#status-codes)

---

## 🔑 Authentication Endpoints

### Register New User

**Endpoint:** `POST /auth/register`  
**Authentication:** ❌ Not required  
**Status:** `201 Created`

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john@college.edu",
  "firstName": "John",
  "lastName": "Doe",
  "password": "SecurePass123!",
  "confirmPassword": "SecurePass123!",
  "college": "MIT",
  "phoneNumber": "9876543210",
  "studentId": "MIT2024001",
  "acceptTerms": true
}
```

**Response (201):**
```json
{
  "userId": 1,
  "username": "john_doe",
  "email": "john@college.edu",
  "fullName": "John Doe",
  "role": "STUDENT",
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "emailVerified": false,
  "studentVerified": false
}
```

**Validation Rules:**
- ✅ Username: 3-50 characters
- ✅ Email: Valid email format
- ✅ Password: 8-50 characters, must match confirmPassword
- ✅ First/Last Name: 2-100 characters each
- ✅ College: 2-100 characters
- ✅ Phone: 10-20 characters

---

### Login User

**Endpoint:** `POST /auth/login`  
**Authentication:** ❌ Not required  
**Status:** `200 OK`

**Request Body:**
```json
{
  "email": "john@college.edu",
  "password": "SecurePass123!"
}
```

**Response (200):**
```json
{
  "userId": 1,
  "username": "john_doe",
  "email": "john@college.edu",
  "fullName": "John Doe",
  "profilePhotoUrl": "https://...",
  "role": "STUDENT",
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "emailVerified": true,
  "studentVerified": false
}
```

**Error (401):**
```json
{
  "status": 401,
  "error": "INVALID_CREDENTIALS",
  "message": "Invalid email or password",
  "timestamp": "2026-06-06T10:30:00"
}
```

---

### Refresh Token

**Endpoint:** `POST /auth/refresh`  
**Authentication:** ✅ Required (Refresh Token)  
**Status:** `200 OK`

**Request:**
```
Authorization: Bearer YOUR_REFRESH_TOKEN_HERE
```

**Response (200):**
```json
{
  "userId": 1,
  "username": "john_doe",
  "email": "john@college.edu",
  "fullName": "John Doe",
  "accessToken": "NEW_ACCESS_TOKEN",
  "refreshToken": "NEW_REFRESH_TOKEN",
  "expiresIn": 86400000
}
```

---

### Verify Email

**Endpoint:** `POST /auth/verify-email`  
**Authentication:** ✅ Required  
**Status:** `200 OK`

**Request:**
```
POST /auth/verify-email?email=john@college.edu
```

**Response (200):**
```json
{
  "message": "Email verified successfully"
}
```

---

### Verify Student

**Endpoint:** `POST /auth/verify-student`  
**Authentication:** ✅ Required  
**Status:** `200 OK`

**Request:**
```
POST /auth/verify-student?email=john@college.edu
```

**Response (200):**
```json
{
  "message": "Student verified successfully"
}
```

---

### Logout

**Endpoint:** `POST /auth/logout`  
**Authentication:** ✅ Required  
**Status:** `200 OK`

**Response (200):**
```json
{
  "message": "Logged out successfully. Please delete the token on client side."
}
```

---

## 🛍️ Product Endpoints

### Get All Products

**Endpoint:** `GET /products`  
**Authentication:** ❌ Not required  
**Status:** `200 OK`

**Query Parameters:**
- `page` (int, default: 0) - Page number
- `size` (int, default: 12) - Items per page

**Request:**
```
GET /products?page=0&size=12
```

**Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "MacBook Pro 2023",
      "description": "Like new condition...",
      "price": 50000,
      "originalPrice": 80000,
      "category": "ELECTRONICS",
      "condition": "LIKE_NEW",
      "status": "AVAILABLE",
      "sellerId": 5,
      "sellerName": "John Seller",
      "sellerRating": 4.8,
      "college": "MIT",
      "location": "Building A",
      "imageUrls": ["url1", "url2"],
      "viewCount": 45,
      "rating": 4.5,
      "totalReviews": 8,
      "createdAt": "2026-06-01T10:30:00",
      "updatedAt": "2026-06-05T15:45:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 12,
    "totalElements": 150,
    "totalPages": 13
  }
}
```

---

### Get Product by ID

**Endpoint:** `GET /products/{id}`  
**Authentication:** ❌ Not required  
**Status:** `200 OK`

**Request:**
```
GET /products/1
```

**Response (200):**
```json
{
  "id": 1,
  "title": "MacBook Pro 2023",
  "description": "Like new condition...",
  "price": 50000,
  "originalPrice": 80000,
  "category": "ELECTRONICS",
  "condition": "LIKE_NEW",
  "status": "AVAILABLE",
  "sellerId": 5,
  "sellerName": "John Seller",
  "sellerRating": 4.8,
  "college": "MIT",
  "location": "Building A",
  "imageUrls": ["url1", "url2"],
  "viewCount": 46,
  "rating": 4.5,
  "totalReviews": 8,
  "createdAt": "2026-06-01T10:30:00",
  "updatedAt": "2026-06-05T15:45:00"
}
```

---

### Create Product

**Endpoint:** `POST /products`  
**Authentication:** ✅ Required  
**Status:** `201 Created`

**Request Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
X-User-Id: 1 (seller ID)
```

**Request Body:**
```json
{
  "title": "MacBook Pro 2023",
  "description": "Like new MacBook Pro with 16GB RAM and 512GB SSD. Purchased 6 months ago.",
  "price": 50000,
  "originalPrice": 80000,
  "category": "ELECTRONICS",
  "condition": "LIKE_NEW",
  "college": "MIT",
  "location": "Building A, Room 101",
  "imageUrls": ["url1", "url2", "url3"],
  "videoUrl": "video_url",
  "modelUrl": "model_url"
}
```

**Response (201):**
```json
{
  "id": 1,
  "title": "MacBook Pro 2023",
  "description": "Like new MacBook Pro with 16GB RAM and 512GB SSD...",
  "price": 50000,
  "originalPrice": 80000,
  "category": "ELECTRONICS",
  "condition": "LIKE_NEW",
  "status": "AVAILABLE",
  "sellerId": 1,
  "sellerName": "John Doe",
  "sellerRating": 0,
  "college": "MIT",
  "location": "Building A, Room 101",
  "imageUrls": ["url1", "url2", "url3"],
  "viewCount": 0,
  "rating": 0,
  "totalReviews": 0,
  "createdAt": "2026-06-06T10:30:00",
  "updatedAt": "2026-06-06T10:30:00"
}
```

---

### Update Product

**Endpoint:** `PUT /products/{id}`  
**Authentication:** ✅ Required  
**Status:** `200 OK`

**Request:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
X-User-Id: 1 (must be product owner)
```

**Request Body:** (Same as Create Product)

---

### Delete Product

**Endpoint:** `DELETE /products/{id}`  
**Authentication:** ✅ Required  
**Status:** `200 OK`

**Request:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
X-User-Id: 1 (must be product owner)
```

**Response (200):**
```json
{
  "message": "Product deleted successfully"
}
```

---

### Search Products by Category

**Endpoint:** `GET /products/category/{category}`  
**Authentication:** ❌ Not required  
**Status:** `200 OK`

**Categories:**
```
ELECTRONICS, BOOKS, FURNITURE, VEHICLES, FASHION, ACCESSORIES,
SPORTS, GADGETS, GAMING, HOSTEL_ESSENTIALS, COLLEGE_ACCESSORIES,
STATIONERY, STUDY_MATERIALS, NOTES_ACADEMIC_MATERIALS, OTHER
```

**Request:**
```
GET /products/category/ELECTRONICS?page=0&size=12
```

---

### Search Products by College

**Endpoint:** `GET /products/college/{college}`  
**Authentication:** ❌ Not required  
**Status:** `200 OK`

**Request:**
```
GET /products/college/MIT?page=0&size=12
```

---

### Search Products

**Endpoint:** `GET /products/search`  
**Authentication:** ❌ Not required  
**Status:** `200 OK`

**Query Parameters:**
- `query` (string) - Search keyword

**Request:**
```
GET /products/search?query=laptop&page=0&size=12
```

---

### Advanced Search

**Endpoint:** `GET /products/search/advanced`  
**Authentication:** ❌ Not required  
**Status:** `200 OK`

**Query Parameters:**
- `minPrice` (decimal) - Minimum price
- `maxPrice` (decimal) - Maximum price
- `category` (string) - Product category
- `college` (string) - College name
- `page` (int, default: 0)
- `size` (int, default: 12)

**Request:**
```
GET /products/search/advanced?minPrice=10000&maxPrice=100000&category=ELECTRONICS&college=MIT&page=0&size=12
```

---

### Get Seller's Products

**Endpoint:** `GET /products/seller/{sellerId}`  
**Authentication:** ❌ Not required  
**Status:** `200 OK`

**Request:**
```
GET /products/seller/5?page=0&size=12
```

---

### Mark Product as Sold

**Endpoint:** `PATCH /products/{id}/mark-sold`  
**Authentication:** ✅ Required  
**Status:** `200 OK`

**Request:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
X-User-Id: 1 (must be product owner)
```

**Response (200):**
```json
{
  "message": "Product marked as sold"
}
```

---

## 👤 User Endpoints

### Get User Profile

**Endpoint:** `GET /users/{id}`  
**Authentication:** ❌ Not required  
**Status:** `200 OK`

**Request:**
```
GET /users/1
```

**Response (200):**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@college.edu",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "9876543210",
  "college": "MIT",
  "studentId": "MIT2024001",
  "profilePhotoUrl": "https://...",
  "bio": "Student at MIT, interested in tech products",
  "role": "STUDENT",
  "rating": 4.8,
  "totalReviews": 12,
  "emailVerified": true,
  "studentVerified": true,
  "isActive": true,
  "createdAt": "2026-05-01T10:30:00",
  "lastLogin": "2026-06-06T10:30:00"
}
```

---

### Update User Profile

**Endpoint:** `PUT /users/{id}`  
**Authentication:** ✅ Required  
**Status:** `200 OK`

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "9876543210",
  "profilePhotoUrl": "https://...",
  "bio": "Updated bio"
}
```

---

### Get User's Products

**Endpoint:** `GET /users/{id}/products`  
**Authentication:** ❌ Not required  
**Status:** `200 OK`

**Request:**
```
GET /users/1/products?page=0&size=12
```

---

### Get User's Reviews

**Endpoint:** `GET /users/{id}/reviews`  
**Authentication:** ❌ Not required  
**Status:** `200 OK`

**Request:**
```
GET /users/1/reviews?page=0&size=10
```

---

### Get User Statistics

**Endpoint:** `GET /users/{id}/stats`  
**Authentication:** ❌ Not required  
**Status:** `200 OK`

**Response (200):**
```json
{
  "userId": 1,
  "username": "john_doe",
  "fullName": "John Doe",
  "college": "MIT",
  "role": "STUDENT",
  "activeListings": 5,
  "totalReviews": 12,
  "averageRating": 4.8,
  "emailVerified": true,
  "studentVerified": true,
  "joinedDate": "2026-05-01T10:30:00"
}
```

---

### Search Users

**Endpoint:** `GET /users/search`  
**Authentication:** ❌ Not required  
**Status:** `200 OK`

**Query Parameters:**
- `name` (string) - User name to search

**Request:**
```
GET /users/search?name=john&page=0&size=20
```

---

### Get Users by College

**Endpoint:** `GET /users/college/{college}`  
**Authentication:** ❌ Not required  
**Status:** `200 OK`

**Request:**
```
GET /users/college/MIT?page=0&size=20
```

---

## ⭐ Review Endpoints

### Create Review

**Endpoint:** `POST /reviews`  
**Authentication:** ✅ Required  
**Status:** `201 Created`

**Request Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
X-User-Id: 1 (reviewer ID)
```

**Request Body:**
```json
{
  "productId": 1,
  "sellerId": 5,
  "rating": 4.5,
  "title": "Great product, fast shipping",
  "comment": "Excellent quality! The product is exactly as described. Seller responded quickly to my questions.",
  "isVerifiedPurchase": true
}
```

**Response (201):**
```json
{
  "id": 1,
  "productId": 1,
  "productTitle": "MacBook Pro 2023",
  "reviewerId": 1,
  "reviewerName": "John Doe",
  "sellerId": 5,
  "sellerName": "Jane Seller",
  "rating": 4.5,
  "title": "Great product, fast shipping",
  "comment": "Excellent quality! The product is exactly as described...",
  "isVerifiedPurchase": true,
  "helpfulCount": 0,
  "unhelpfulCount": 0,
  "createdAt": "2026-06-06T10:30:00",
  "updatedAt": "2026-06-06T10:30:00"
}
```

---

### Get Product Reviews

**Endpoint:** `GET /reviews/product/{productId}`  
**Authentication:** ❌ Not required  
**Status:** `200 OK`

**Request:**
```
GET /reviews/product/1?page=0&size=10
```

---

### Get Seller Reviews

**Endpoint:** `GET /reviews/seller/{sellerId}`  
**Authentication:** ❌ Not required  
**Status:** `200 OK`

**Request:**
```
GET /reviews/seller/5?page=0&size=10
```

---

### Update Review

**Endpoint:** `PUT /reviews/{id}`  
**Authentication:** ✅ Required  
**Status:** `200 OK`

**Request Body:** (Same as Create Review)

---

### Delete Review

**Endpoint:** `DELETE /reviews/{id}`  
**Authentication:** ✅ Required  
**Status:** `200 OK`

---

## 💬 Message Endpoints

### Send Message

**Endpoint:** `POST /messages/send`  
**Authentication:** ✅ Required  
**Status:** `201 Created`

**Request Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
X-User-Id: 1 (sender ID)
```

**Request Body:**
```json
{
  "recipientId": 5,
  "content": "Hi, are you still selling the MacBook Pro?",
  "imageUrl": null,
  "voiceNoteUrl": null
}
```

**Response (201):**
```json
{
  "id": 1,
  "senderId": 1,
  "senderName": "John Doe",
  "recipientId": 5,
  "recipientName": "Jane Seller",
  "content": "Hi, are you still selling the MacBook Pro?",
  "imageUrl": null,
  "voiceNoteUrl": null,
  "isRead": false,
  "createdAt": "2026-06-06T10:30:00",
  "readAt": null
}
```

---

### Get Conversations

**Endpoint:** `GET /messages/conversations`  
**Authentication:** ✅ Required  
**Status:** `200 OK`

**Request:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
X-User-Id: 1
```

**Response (200):**
```json
[
  {
    "userId": 5,
    "username": "jane_seller",
    "fullName": "Jane Seller",
    "profilePhotoUrl": "https://...",
    "unreadCount": 2
  },
  {
    "userId": 8,
    "username": "bob_buyer",
    "fullName": "Bob Buyer",
    "profilePhotoUrl": "https://...",
    "unreadCount": 0
  }
]
```

---

### Get Conversation History

**Endpoint:** `GET /messages/conversation/{partnerId}`  
**Authentication:** ✅ Required  
**Status:** `200 OK`

**Request:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
X-User-Id: 1
GET /messages/conversation/5?page=0&size=20
```

---

### Get All Messages

**Endpoint:** `GET /messages`  
**Authentication:** ✅ Required  
**Status:** `200 OK`

**Request:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
X-User-Id: 1
GET /messages?page=0&size=20
```

---

### Get Unread Messages

**Endpoint:** `GET /messages/unread`  
**Authentication:** ✅ Required  
**Status:** `200 OK`

**Request:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
X-User-Id: 1
```

---

### Mark Message as Read

**Endpoint:** `PUT /messages/{id}/read`  
**Authentication:** ✅ Required  
**Status:** `200 OK`

---

### Delete Message

**Endpoint:** `DELETE /messages/{id}`  
**Authentication:** ✅ Required  
**Status:** `200 OK`

---

## 🚨 Error Handling

All error responses follow this format:

```json
{
  "timestamp": "2026-06-06T10:30:00",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "errors": {
    "fieldName": "Field error message"
  },
  "path": "/api/endpoint"
}
```

### Common Error Codes

| Error Code | Status | Description |
|-----------|--------|-------------|
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `INVALID_CREDENTIALS` | 401 | Wrong email or password |
| `AUTHENTICATION_FAILED` | 401 | Authentication token invalid |
| `PERMISSION_DENIED` | 403 | User doesn't have permission |
| `USER_NOT_FOUND` | 404 | User doesn't exist |
| `PRODUCT_NOT_FOUND` | 404 | Product doesn't exist |
| `EMAIL_EXISTS` | 409 | Email already registered |
| `USERNAME_EXISTS` | 409 | Username already taken |
| `REVIEW_EXISTS` | 409 | Already reviewed this product |
| `INTERNAL_SERVER_ERROR` | 500 | Server error |

---

## ✅ Status Codes

| Code | Meaning |
|------|---------|
| `200` | OK - Request successful |
| `201` | Created - Resource created |
| `400` | Bad Request - Invalid input |
| `401` | Unauthorized - Authentication required |
| `403` | Forbidden - Permission denied |
| `404` | Not Found - Resource not found |
| `409` | Conflict - Resource already exists |
| `500` | Internal Server Error |

---

## 🧪 Testing the API

### Using cURL

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@mit.edu","firstName":"John","lastName":"Doe","password":"Pass123!","confirmPassword":"Pass123!","college":"MIT","phoneNumber":"1234567890"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@mit.edu","password":"Pass123!"}'

# Get all products
curl -X GET "http://localhost:8080/api/products?page=0&size=12"

# Create product (with token)
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "X-User-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{"title":"Laptop","description":"Good condition","price":50000,"category":"ELECTRONICS","condition":"GOOD","college":"MIT","location":"Campus"}'
```

### Using Postman

1. Import the OpenAPI spec from `http://localhost:8080/v3/api-docs`
2. Set the authorization type to **Bearer Token**
3. Paste your access token
4. Send requests

---

## 📚 Additional Resources

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs
- **Health Check:** http://localhost:8080/actuator/health
- **Metrics:** http://localhost:8080/actuator/metrics

---

**Last Updated:** 2026-06-06  
**Created by:** CampusXchange Team

package com.campusxchange.config;

import com.campusxchange.entity.*;
import com.campusxchange.repository.ProductRepository;
import com.campusxchange.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
public class DataInitializer implements ApplicationRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (productRepository.count() > 0) return;

        log.info("Seeding demo data...");

        User seller1 = getOrCreateUser("arjun.sharma@iitb.ac.in", "arjunsharma", "Arjun", "Sharma", "IIT Bombay");
        User seller2 = getOrCreateUser("priya.nair@bits.ac.in", "priyanair", "Priya", "Nair", "BITS Pilani");
        User seller3 = getOrCreateUser("rohit.verma@nitk.ac.in", "rohitverma", "Rohit", "Verma", "NIT Karnataka");

        List<Product> products = List.of(
            product("MacBook Pro M2 14\"", "Barely used MacBook Pro M2, 16GB RAM, 512GB SSD. Got a company laptop so selling this. Comes with original charger and box.", new BigDecimal("85000"), new BigDecimal("129900"), ProductCategory.ELECTRONICS, ProductCondition.LIKE_NEW, seller1, "IIT Bombay", "[\"https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800\",\"https://images.unsplash.com/photo-1611186871525-b4eee0a9e264?w=800\"]"),
            product("Sony WH-1000XM5 Headphones", "Premium noise-cancelling headphones. Used for 3 months. Excellent sound quality, battery lasts 30 hours. With case and original cable.", new BigDecimal("18500"), new BigDecimal("29990"), ProductCategory.ELECTRONICS, ProductCondition.GOOD, seller2, "BITS Pilani", "[\"https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800\",\"https://images.unsplash.com/photo-1583394838336-acd977736f90?w=800\"]"),
            product("Engineering Mathematics by RD Sharma", "Complete set for first and second year engineering. Excellent condition, very few notes in margins. All chapters intact.", new BigDecimal("450"), new BigDecimal("850"), ProductCategory.BOOKS, ProductCondition.GOOD, seller3, "NIT Karnataka", "[\"https://images.unsplash.com/photo-1512820790803-83ca734da794?w=800\",\"https://images.unsplash.com/photo-1476275466078-4007374efbbe?w=800\"]"),
            product("Study Table with Drawers", "Solid wood study table with two drawers and a shelf. 4ft × 2.5ft. Perfect for hostel room. Self-pickup only from hostel H3.", new BigDecimal("3200"), new BigDecimal("7000"), ProductCategory.FURNITURE, ProductCondition.GOOD, seller1, "IIT Bombay", "[\"https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=800\",\"https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=800\"]"),
            product("GATE 2024 Preparation Books (CS)", "Complete GATE CSE set: Made Easy + ACE Academy notes for all subjects. Very clean, no scribbles. Sold in lot only.", new BigDecimal("1800"), new BigDecimal("4500"), ProductCategory.STUDY_MATERIALS, ProductCondition.LIKE_NEW, seller2, "BITS Pilani", "[\"https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=800\",\"https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=800\"]"),
            product("Mechanical Keyboard — Keychron K2", "Keychron K2 V2 with Red switches. RGB backlit, compact TKL layout. Bluetooth + USB-C. Like new condition.", new BigDecimal("5500"), new BigDecimal("8500"), ProductCategory.ELECTRONICS, ProductCondition.LIKE_NEW, seller3, "NIT Karnataka", "[\"https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800\",\"https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?w=800\"]"),
            product("Cycle — Hero Sprint 26T", "Black Hero Sprint mountain bike. 21-speed Shimano gears. Tyres replaced 2 months ago. Minor scratches on frame, rides perfectly.", new BigDecimal("4500"), new BigDecimal("9500"), ProductCategory.VEHICLES, ProductCondition.FAIR, seller1, "IIT Bombay", "[\"https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=800\",\"https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=800\"]"),
            product("iPad 10th Gen + Apple Pencil", "iPad 10th generation 64GB WiFi with Apple Pencil (1st gen). Used for a semester of notes. Screen protector on, no scratches.", new BigDecimal("38000"), new BigDecimal("58000"), ProductCategory.ELECTRONICS, ProductCondition.GOOD, seller2, "BITS Pilani", "[\"https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=800\",\"https://images.unsplash.com/photo-1561154464-82e9adf32764?w=800\"]"),
            product("Induction Cooktop + Cookware Set", "Prestige induction cooktop (1500W) with a 3-piece cookware set (kadai, tawa, pot). Hostel-friendly. Works perfectly.", new BigDecimal("2200"), new BigDecimal("4800"), ProductCategory.HOSTEL_ESSENTIALS, ProductCondition.GOOD, seller3, "NIT Karnataka", "[\"https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=800\",\"https://images.unsplash.com/photo-1590422749897-47726d5c9e6b?w=800\"]"),
            product("PS5 Controller — DualSense", "Sony DualSense controller, barely used. Haptic feedback and adaptive triggers work perfectly. White colour. No scratches.", new BigDecimal("5800"), new BigDecimal("7490"), ProductCategory.GAMING, ProductCondition.LIKE_NEW, seller1, "IIT Bombay", "[\"https://images.unsplash.com/photo-1606144042614-b2417e99c4e3?w=800\",\"https://images.unsplash.com/photo-1607853202273-797f1c22a38e?w=800\"]"),
            product("Quantum Mechanics Handwritten Notes", "Complete handwritten notes for Quantum Mechanics (PHY301). Covers all exam topics with solved examples and previous year questions.", new BigDecimal("350"), new BigDecimal("0"), ProductCategory.NOTES_ACADEMIC_MATERIALS, ProductCondition.GOOD, seller2, "BITS Pilani", "[\"https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=800\",\"https://images.unsplash.com/photo-1501504905252-473c47e087f8?w=800\"]"),
            product("Nike Air Max 270 Sneakers (Size 9)", "Nike Air Max 270 in white/volt. Size UK 9 / US 10. Worn only twice for a college event. Original box included.", new BigDecimal("4200"), new BigDecimal("11995"), ProductCategory.FASHION, ProductCondition.LIKE_NEW, seller3, "NIT Karnataka", "[\"https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800\",\"https://images.unsplash.com/photo-1600269452121-4f2416e55c28?w=800\"]")
        );

        productRepository.saveAll(products);
        log.info("Seeded {} demo products.", products.size());
    }

    private User getOrCreateUser(String email, String username, String firstName, String lastName, String college) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = User.builder()
                    .email(email)
                    .username(username)
                    .firstName(firstName)
                    .lastName(lastName)
                    .password(passwordEncoder.encode("demo1234"))
                    .college(college)
                    .role(UserRole.STUDENT)
                    .emailVerified(true)
                    .studentVerified(true)
                    .isActive(true)
                    .rating(4.5)
                    .totalReviews(0)
                    .build();
            return userRepository.save(u);
        });
    }

    private Product product(String title, String desc, BigDecimal price, BigDecimal originalPrice,
                             ProductCategory category, ProductCondition condition,
                             User seller, String college, String imageUrls) {
        return Product.builder()
                .title(title)
                .description(desc)
                .price(price)
                .originalPrice(originalPrice.compareTo(BigDecimal.ZERO) > 0 ? originalPrice : null)
                .category(category)
                .condition(condition)
                .status(ProductStatus.AVAILABLE)
                .seller(seller)
                .college(college)
                .location(college)
                .imageUrls(imageUrls)
                .viewCount(0)
                .rating(BigDecimal.ZERO)
                .totalReviews(0)
                .build();
    }
}

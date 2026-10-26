package com.example.hireme.data

/**
 * User role determines which home screen is shown after login.
 */
enum class UserRole {
    CUSTOMER,
    WORKER
}

/**
 * Represents a registered user (customer or worker).
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val rating: Float = 0f
)

/**
 * Standardized Category IDs for database persistence & navigation.
 */
object CategoryIds {
    const val HOME_CARE = "home_care"
    const val BEAUTY = "beauty"
    const val HEALTH_CARE = "health_care"
    const val APPLIANCES = "appliances"
    const val ELECTRIC = "electric"
    const val PLUMBING = "plumbing"
    const val AUTOMOTIVE = "automotive"
    const val FURNITURE = "furniture"
    const val INSTALLATION = "installation"
}

/**
 * Top-level service category (e.g. "Home Care", "Automotive", etc.)
 */
data class ServiceCategory(
    val id: String,
    val title: String,
    val description: String = "",
    val iconKey: String = id
)

/**
 * A sub-service within a category (e.g., "Deep Cleaning" under "Home Care").
 */
data class SubService(
    val id: String,
    val categoryId: String,
    val name: String,
    val description: String,
    val basePrice: Int, // in ₹
    val rating: Float = 4.8f,
    val reviewCount: Int = 120,
    val duration: String = "1-2 hrs",
    val tag: String = "Popular"
)

/**
 * Tracks the lifecycle of a service request.
 *
 * PENDING   → customer created, waiting for worker
 * ACCEPTED  → worker accepted the request
 * IN_PROGRESS → worker is actively working (optional intermediate)
 * COMPLETED → worker marked work as done
 * RATED     → customer submitted a rating
 * PAID      → customer paid the HireMe platform
 */
enum class RequestStatus {
    PENDING,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    RATED,
    PAID
}

/**
 * A service request created by a customer.
 */
data class ServiceRequest(
    val id: String,
    val customerId: String,
    val customerName: String,
    val workerId: String? = null,
    val workerName: String? = null,
    val subService: SubService,
    val status: RequestStatus = RequestStatus.PENDING,
    val address: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Customer feedback after work is completed.
 */
data class Rating(
    val requestId: String,
    val score: Int, // 1-5 stars
    val comment: String = ""
)

/**
 * Payment record for a completed service.
 */
data class Payment(
    val requestId: String,
    val amount: Int, // in ₹
    val method: String = "", // UPI, Card, Wallet
    val isPaid: Boolean = false
)

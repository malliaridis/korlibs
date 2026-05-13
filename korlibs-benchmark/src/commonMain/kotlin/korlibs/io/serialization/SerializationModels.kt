package korlibs.io.serialization

import kotlinx.serialization.Serializable

// --- Simple flat object ---
@Serializable
data class SimpleUser(
    val id: Int,
    val name: String,
    val email: String,
    val active: Boolean,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id, "name" to name, "email" to email, "active" to active
    )
    companion object {
        fun fromMap(m: Map<*, *>) = SimpleUser(
            id     = (m["id"] as Int),
            name   = m["name"] as String,
            email  = m["email"] as String,
            active = m["active"] as Boolean,
        )
    }
}

// --- Nested object ---
@Serializable
data class Address(
    val street: String,
    val city: String,
    val country: String,
    val zip: String,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "street" to street, "city" to city, "country" to country, "zip" to zip
    )
    companion object {
        fun fromMap(m: Map<*, *>) = Address(
            street  = m["street"] as String,
            city    = m["city"] as String,
            country = m["country"] as String,
            zip     = m["zip"] as String,
        )
    }
}

@Serializable
data class UserWithAddress(
    val id: Int,
    val name: String,
    val email: String,
    val address: Address,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id, "name" to name, "email" to email, "address" to address.toMap()
    )
    companion object {
        fun fromMap(m: Map<*, *>) = UserWithAddress(
            id      = m["id"] as Int,
            name    = m["name"] as String,
            email   = m["email"] as String,
            address = Address.fromMap(m["address"] as Map<*, *>),
        )
    }
}

// --- List-heavy object ---
@Serializable
data class Order(
    val orderId: String,
    val customerId: Int,
    val items: List<OrderItem>,
    val tags: List<String>,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "orderId"    to orderId,
        "customerId" to customerId,
        "items"      to items.map { it.toMap() },
        "tags"       to tags,
    )
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(m: Map<*, *>) = Order(
            orderId    = m["orderId"] as String,
            customerId = m["customerId"] as Int,
            items      = (m["items"] as List<*>).map { OrderItem.fromMap(it as Map<*, *>) },
            tags       = (m["tags"] as List<*>).map { it as String },
        )
    }
}

@Serializable
data class OrderItem(
    val sku: String,
    val quantity: Int,
    val priceUsd: Double,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "sku" to sku, "quantity" to quantity, "priceUsd" to priceUsd
    )
    companion object {
        fun fromMap(m: Map<*, *>) = OrderItem(
            sku      = m["sku"] as String,
            quantity = m["quantity"] as Int,
            priceUsd = (m["priceUsd"] as Number).toDouble(),
        )
    }
}

// --- Nullable / optional fields ---
@Serializable
data class Profile(
    val userId: Int,
    val bio: String?,
    val website: String?,
    val avatarUrl: String?,
    val followersCount: Int,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId"         to userId,
        "bio"            to bio,
        "website"        to website,
        "avatarUrl"      to avatarUrl,
        "followersCount" to followersCount,
    )
    companion object {
        fun fromMap(m: Map<*, *>) = Profile(
            userId         = m["userId"] as Int,
            bio            = m["bio"] as? String,
            website        = m["website"] as? String,
            avatarUrl      = m["avatarUrl"] as? String,
            followersCount = m["followersCount"] as Int,
        )
    }
}
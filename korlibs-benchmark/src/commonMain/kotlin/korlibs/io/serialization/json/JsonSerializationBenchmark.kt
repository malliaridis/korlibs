package com.example.benchmarks.serialization

import korlibs.io.serialization.Address
import korlibs.io.serialization.Order
import korlibs.io.serialization.OrderItem
import korlibs.io.serialization.Profile
import korlibs.io.serialization.SimpleUser
import korlibs.io.serialization.UserWithAddress
import korlibs.io.serialization.json.Json
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Measurement
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import kotlinx.serialization.json.Json as KxJson

/**
 * This benchmark is comparing kotlinx serialization with the korlibs serialization performance,
 * specifically on the use-case of serializing and deserializing objects with JSON.
 *
 * Since korlibs does not serialize and deserialize objects, but only maps, lists and primitives,
 * an additional mapping step is required for this use-case.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
open class JsonSerializationBenchmark {

    private val kxJson = KxJson { ignoreUnknownKeys = true }

    // Objects
    private lateinit var simpleUser: SimpleUser
    private lateinit var userWithAddress: UserWithAddress
    private lateinit var order: Order
    private lateinit var profile: Profile
    private lateinit var profileWithNulls: Profile

    // Pre-encoded JSON strings (same input for both decoders — fair comparison)
    private lateinit var simpleUserJson: String
    private lateinit var userWithAddressJson: String
    private lateinit var orderJson: String
    private lateinit var profileJson: String
    private lateinit var profileWithNullsJson: String

    @Setup
    fun setup() {
        simpleUser = SimpleUser(1, "Alice Smith", "alice@example.com", true)
        simpleUserJson = kxJson.encodeToString(simpleUser)

        userWithAddress = UserWithAddress(
            id = 2, name = "Bob Jones", email = "bob@example.com",
            address = Address("123 Main St", "Springfield", "US", "62701"),
        )
        userWithAddressJson = kxJson.encodeToString(userWithAddress)

        order = Order(
            orderId = "ORD-9999", customerId = 42,
            items = List(20) { i -> OrderItem("SKU-$i", i + 1, (i + 1) * 9.99) },
            tags = listOf("urgent", "fragile", "international"),
        )
        orderJson = kxJson.encodeToString(order)

        profile = Profile(7, "Engineer and OSS fan.", "https://alice.dev", "https://cdn.example.com/avatar.png", 1_234)
        profileJson = kxJson.encodeToString(profile)

        profileWithNulls = Profile(8, null, null, null, 0)
        profileWithNullsJson = kxJson.encodeToString(profileWithNulls)
    }

    // ── Encode: Simple ────────────────────────────────────────────────────────

    /** korlibs: object → Map → JSON string */
    @Benchmark
    fun korlibs_encode_simple(): String = Json.stringify(simpleUser.toMap())

    /** kotlinx: object → JSON string (via generated serializer) */
    @Benchmark
    fun kotlinx_encode_simple(): String = kxJson.encodeToString(simpleUser)

    // ── Decode: Simple ────────────────────────────────────────────────────────

    /** korlibs: JSON string → Map → object */
    @Benchmark
    fun korlibs_decode_simple(): SimpleUser =
        SimpleUser.fromMap(Json.parse(simpleUserJson) as Map<*, *>)

    /** kotlinx: JSON string → object (via generated serializer) */
    @Benchmark
    fun kotlinx_decode_simple(): SimpleUser =
        kxJson.decodeFromString(simpleUserJson)

    // ── Encode: Nested ────────────────────────────────────────────────────────

    @Benchmark
    fun korlibs_encode_nested(): String = Json.stringify(userWithAddress.toMap())

    @Benchmark
    fun kotlinx_encode_nested(): String = kxJson.encodeToString(userWithAddress)

    // ── Decode: Nested ────────────────────────────────────────────────────────

    @Benchmark
    fun korlibs_decode_nested(): UserWithAddress =
        UserWithAddress.fromMap(Json.parse(userWithAddressJson) as Map<*, *>)

    @Benchmark
    fun kotlinx_decode_nested(): UserWithAddress =
        kxJson.decodeFromString(userWithAddressJson)

    // ── Encode: List-heavy (20 items) ─────────────────────────────────────────

    @Benchmark
    fun korlibs_encode_list(): String = Json.stringify(order.toMap())

    @Benchmark
    fun kotlinx_encode_list(): String = kxJson.encodeToString(order)

    // ── Decode: List-heavy ────────────────────────────────────────────────────

    @Benchmark
    fun korlibs_decode_list(): Order =
        Order.fromMap(Json.parse(orderJson) as Map<*, *>)

    @Benchmark
    fun kotlinx_decode_list(): Order =
        kxJson.decodeFromString(orderJson)

    // ── Encode: Nullable fields (all present) ─────────────────────────────────

    @Benchmark
    fun korlibs_encode_nullable_full(): String = Json.stringify(profile.toMap())

    @Benchmark
    fun kotlinx_encode_nullable_full(): String = kxJson.encodeToString(profile)

    // ── Decode: Nullable fields (all present) ─────────────────────────────────

    @Benchmark
    fun korlibs_decode_nullable_full(): Profile =
        Profile.fromMap(Json.parse(profileJson) as Map<*, *>)

    @Benchmark
    fun kotlinx_decode_nullable_full(): Profile =
        kxJson.decodeFromString(profileJson)

    // ── Encode: Nullable fields (all null) ────────────────────────────────────

    @Benchmark
    fun korlibs_encode_nullable_nulls(): String = Json.stringify(profileWithNulls.toMap())

    @Benchmark
    fun kotlinx_encode_nullable_nulls(): String = kxJson.encodeToString(profileWithNulls)

    // ── Decode: Nullable fields (all null) ────────────────────────────────────

    @Benchmark
    fun korlibs_decode_nullable_nulls(): Profile =
        Profile.fromMap(Json.parse(profileWithNullsJson) as Map<*, *>)

    @Benchmark
    fun kotlinx_decode_nullable_nulls(): Profile =
        kxJson.decodeFromString(profileWithNullsJson)
}

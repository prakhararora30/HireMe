package com.example.hireme.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Repository interface for the HireMe service workflow.
 * Backed by in-memory data for now — swap in Firebase/Room later.
 */
interface HireMeRepository {

    // ── Auth ──────────────────────────────────────────────────────────
    fun getCurrentUser(): User?
    fun login(email: String, password: String, role: UserRole): User
    fun register(name: String, email: String, password: String, role: UserRole): User
    fun logout()

    // ── Categories & Sub-Services ─────────────────────────────────────
    fun getCategories(): Flow<List<ServiceCategory>>
    fun getCategoryById(categoryId: String): ServiceCategory?
    fun getSubServices(categoryId: String): Flow<List<SubService>>
    fun getAllSubServices(): Flow<List<SubService>>

    // ── Requests ──────────────────────────────────────────────────────
    fun createRequest(
        subService: SubService,
        address: String,
        description: String
    ): ServiceRequest

    /** All PENDING requests (for workers to browse). */
    fun getPendingRequests(): Flow<List<ServiceRequest>>

    /** Requests belonging to the current customer. */
    fun getMyRequests(): Flow<List<ServiceRequest>>

    /** Requests accepted by the current worker. */
    fun getMyWorkerRequests(): Flow<List<ServiceRequest>>

    fun getRequestById(requestId: String): ServiceRequest?
    fun acceptRequest(requestId: String): ServiceRequest?
    fun completeRequest(requestId: String): ServiceRequest?

    // ── Rating & Payment ──────────────────────────────────────────────
    fun submitRating(requestId: String, score: Int, comment: String): ServiceRequest?
    fun makePayment(requestId: String, method: String): ServiceRequest?
}

// ─────────────────────────────────────────────────────────────────────────────
// In-Memory Implementation
// ─────────────────────────────────────────────────────────────────────────────

class InMemoryHireMeRepository : HireMeRepository {

    // ── State stores ─────────────────────────────────────────────────
    private val defaultUser = User("u_prakhar", "Prakhar", "7705023222", UserRole.CUSTOMER)
    private var currentUser: User? = defaultUser
    private val users = mutableListOf<User>(defaultUser)
    private val requests = MutableStateFlow<List<ServiceRequest>>(emptyList())
    private val ratings = mutableListOf<Rating>()

    // ── Seeded Categories ────────────────────────────────────────────
    private val categoriesList: List<ServiceCategory> = listOf(
        ServiceCategory(CategoryIds.HOME_CARE, "Home Care", "Deep cleaning, kitchen, bathroom, cook & utensil services"),
        ServiceCategory(CategoryIds.BEAUTY, "Beauty", "Haircut, facial, manicure, pedicure & bridal makeup"),
        ServiceCategory(CategoryIds.HEALTH_CARE, "Health Care", "Physiotherapy, nursing, doctor consultation & blood tests"),
        ServiceCategory(CategoryIds.APPLIANCES, "Appliances", "AC repair, washing machine & refrigerator servicing"),
        ServiceCategory(CategoryIds.ELECTRIC, "Electric", "Wiring, inverter & UPS, switchboard, fan & light repairs"),
        ServiceCategory(CategoryIds.PLUMBING, "Plumbing", "Leak repair, pipe fitting, drain cleaning & toilet fixes"),
        ServiceCategory(CategoryIds.AUTOMOTIVE, "Automotive", "Driver, car wash, bike service, tyres & jumpstart"),
        ServiceCategory(CategoryIds.FURNITURE, "Furniture", "Repair, cleaning, refurbishing & assembly/installation"),
        ServiceCategory(CategoryIds.INSTALLATION, "Installation", "AC drilling & mount, RO purifier & fan/light installation")
    )

    // ── Seeded Sub-Services (per category) ───────────────────────────
    private val subServices: Map<String, List<SubService>> = mapOf(
        CategoryIds.HOME_CARE to listOf(
            SubService("hc_1", CategoryIds.HOME_CARE, "Deep Cleaning", "Full home deep-clean for rooms, floors, windows & balcony", 1499),
            SubService("hc_2", CategoryIds.HOME_CARE, "Kitchen Cleaning", "Degreasing cabinets, chimney, gas stove, sink & wall tiles", 799),
            SubService("hc_3", CategoryIds.HOME_CARE, "Bathroom Cleaning", "Tile stain removal, commode scrubbing, fixtures & sanitization", 499),
            SubService("hc_4", CategoryIds.HOME_CARE, "Cook Service", "Professional home cook for breakfast, lunch, dinner & events", 899),
            SubService("hc_5", CategoryIds.HOME_CARE, "Utensil Cleaning", "Complete dishwashing, scrubbing, sink wash & kitchen hygiene", 349)
        ),
        CategoryIds.APPLIANCES to listOf(
            SubService("app_1", CategoryIds.APPLIANCES, "AC Service & Repair", "Jet pump service, gas leak check & cooling problem fix", 699),
            SubService("app_2", CategoryIds.APPLIANCES, "Washing Machine Repair", "Drum issues, drainage blockage, motor fix & spin cycle repair", 499),
            SubService("app_3", CategoryIds.APPLIANCES, "Refrigerator Repair", "Cooling malfunction, thermostat, gas refill & coil cleaning", 599)
        ),
        CategoryIds.ELECTRIC to listOf(
            SubService("elec_1", CategoryIds.ELECTRIC, "Wiring & Cabling", "Internal wiring repair, extension boards & safety check", 599),
            SubService("elec_2", CategoryIds.ELECTRIC, "Inverter and UPS", "Inverter battery check, wiring setup & backup troubleshooting", 499),
            SubService("elec_3", CategoryIds.ELECTRIC, "Switchboard Repair", "Replacement of burnt switches, sockets, MCB & fuse", 299),
            SubService("elec_4", CategoryIds.ELECTRIC, "Fan & Light Repair", "Ceiling fan capacitor, regulator fix & decorative lights", 349)
        ),
        CategoryIds.AUTOMOTIVE to listOf(
            SubService("auto_1", CategoryIds.AUTOMOTIVE, "Driver", "Experienced professional on-demand driver for city or highway", 499),
            SubService("auto_2", CategoryIds.AUTOMOTIVE, "Complete Car Wash", "High-pressure foam exterior wash, interior vacuum & dashboard shine", 699),
            SubService("auto_3", CategoryIds.AUTOMOTIVE, "Two Wheeler Service", "Full bike tune-up, engine oil change, brake & chain lubrication", 449),
            SubService("auto_4", CategoryIds.AUTOMOTIVE, "Tyre & Puncture Service", "On-spot tubeless tyre puncture repair & pressure inspection", 249),
            SubService("auto_5", CategoryIds.AUTOMOTIVE, "Battery Jumpstart & Check", "Emergency vehicle battery jumpstart & charging diagnosis", 399)
        ),
        CategoryIds.BEAUTY to listOf(
            SubService("bty_1", CategoryIds.BEAUTY, "Haircut & Styling", "Professional styling, hair trimming, blow-dry & treatment at home", 499),
            SubService("bty_2", CategoryIds.BEAUTY, "Facial & Cleanup", "Deep pore skin cleanup, anti-tan facial & relaxing massage", 699),
            SubService("bty_3", CategoryIds.BEAUTY, "Manicure & Pedicure", "Nail filing, exfoliating scrub, cuticle care & hand-foot massage", 599),
            SubService("bty_4", CategoryIds.BEAUTY, "Bridal Makeup", "Complete bridal makeup, hairstyling, draping & accessory placement", 4999)
        ),
        CategoryIds.PLUMBING to listOf(
            SubService("plumb_1", CategoryIds.PLUMBING, "Leak Repair", "Fixing leaking faucets, shower valves, overhead tanks & joints", 399),
            SubService("plumb_2", CategoryIds.PLUMBING, "Pipe Installation", "New water line connections, pipeline replacement & drainage pipes", 699),
            SubService("plumb_3", CategoryIds.PLUMBING, "Drain Cleaning", "Clearing clogged sinks, bathroom floor drains & main sewer traps", 499),
            SubService("plumb_4", CategoryIds.PLUMBING, "Toilet Repair", "Flush valve repair, cistern tank leak & jet spray replacement", 349)
        ),
        CategoryIds.HEALTH_CARE to listOf(
            SubService("hlth_1", CategoryIds.HEALTH_CARE, "Physiotherapy Session", "Certified therapist for joint mobility, back pain & stroke rehab", 899),
            SubService("hlth_2", CategoryIds.HEALTH_CARE, "Home Nursing Care", "Trained nurse for post-op dressing, injections & vital signs", 1199),
            SubService("hlth_3", CategoryIds.HEALTH_CARE, "General Physician Consultation", "Doctor home consultation, diagnostic review & medical prescription", 799),
            SubService("hlth_4", CategoryIds.HEALTH_CARE, "Blood Test", "Home sample collection for full body profile & CBC lab testing", 499)
        ),
        CategoryIds.FURNITURE to listOf(
            SubService("furn_1", CategoryIds.FURNITURE, "Furniture Repair", "Fixing loose dining tables, broken chair joints & cabinet hinges", 499),
            SubService("furn_2", CategoryIds.FURNITURE, "Furniture Cleaning", "Deep shampooing for fabric sofas, dining chairs & mattresses", 699),
            SubService("furn_3", CategoryIds.FURNITURE, "Furniture Refurbishing", "Wood sanding, polish, lacquer finish & upholstery re-covering", 1499),
            SubService("furn_4", CategoryIds.FURNITURE, "Furniture Installation", "Assembly of new flat-pack beds, wardrobes, study desks & shelves", 599)
        ),
        CategoryIds.INSTALLATION to listOf(
            SubService("inst_1", CategoryIds.INSTALLATION, "Drilling & AC Installation", "Heavy wall drilling, split/window AC indoor-outdoor unit mounting", 1299),
            SubService("inst_2", CategoryIds.INSTALLATION, "RO Installation", "Water purifier mounting, inlet-outlet plumbing & TDS testing", 499),
            SubService("inst_3", CategoryIds.INSTALLATION, "Fan & Light Installation", "Ceiling fan installation, chandelier, tube lights & wall lamps", 349)
        )
    )

    private var nextRequestId = 1

    // ── Auth ──────────────────────────────────────────────────────────

    override fun getCurrentUser(): User? = currentUser

    override fun login(email: String, password: String, role: UserRole): User {
        // Find existing user by email or phone or name in DB
        val existing = users.find { 
            it.email.equals(email, ignoreCase = true) || 
            it.name.equals(email, ignoreCase = true) ||
            (email.all { char -> char.isDigit() } && it.email.contains(email))
        }
        if (existing != null) {
            currentUser = existing
            return existing
        }
        val raw = email.substringBefore("@")
        val isPhone = raw.all { it.isDigit() } || raw.startsWith("+")
        val displayName = if (isPhone) "Prakhar" else raw.replaceFirstChar { it.uppercase() }
        val user = User(
            id = "u${users.size + 1}",
            name = displayName,
            email = email,
            role = role
        )
        users.add(user)
        currentUser = user
        return user
    }

    override fun register(name: String, email: String, password: String, role: UserRole): User {
        val user = User(
            id = "u${users.size + 1}",
            name = name,
            email = email,
            role = role
        )
        users.add(user)
        currentUser = user
        return user
    }

    override fun logout() {
        currentUser = null
    }

    // ── Categories & Sub-Services ─────────────────────────────────────

    override fun getCategories(): Flow<List<ServiceCategory>> {
        return MutableStateFlow(categoriesList)
    }

    override fun getCategoryById(categoryId: String): ServiceCategory? {
        val resolvedId = resolveCategoryId(categoryId)
        return categoriesList.find { it.id == resolvedId }
    }

    override fun getSubServices(categoryId: String): Flow<List<SubService>> {
        val resolvedId = resolveCategoryId(categoryId)
        return MutableStateFlow(subServices[resolvedId] ?: emptyList())
    }

    override fun getAllSubServices(): Flow<List<SubService>> {
        return MutableStateFlow(subServices.values.flatten())
    }

    private fun resolveCategoryId(id: String): String {
        return when (id.lowercase().trim()) {
            "1", "home", "home_care", "cleaning" -> CategoryIds.HOME_CARE
            "2", "plumbing" -> CategoryIds.PLUMBING
            "3", "electric", "electrician" -> CategoryIds.ELECTRIC
            "4", "painting" -> CategoryIds.HOME_CARE
            "5", "appliances" -> CategoryIds.APPLIANCES
            "6", "moving" -> CategoryIds.AUTOMOTIVE
            "7", "beauty" -> CategoryIds.BEAUTY
            "8", "appliances_repair" -> CategoryIds.APPLIANCES
            "9", "pest_control", "other" -> CategoryIds.HOME_CARE
            "car_bike", "automotive" -> CategoryIds.AUTOMOTIVE
            "computer" -> CategoryIds.APPLIANCES
            "health_care", "healthcare" -> CategoryIds.HEALTH_CARE
            "furniture" -> CategoryIds.FURNITURE
            "installation" -> CategoryIds.INSTALLATION
            else -> id
        }
    }

    // ── Requests ─────────────────────────────────────────────────────

    override fun createRequest(
        subService: SubService,
        address: String,
        description: String
    ): ServiceRequest {
        val user = currentUser ?: error("Must be logged in to create a request")
        val request = ServiceRequest(
            id = "r${nextRequestId++}",
            customerId = user.id,
            customerName = user.name,
            subService = subService,
            address = address,
            description = description
        )
        requests.update { it + request }
        return request
    }

    override fun getPendingRequests(): Flow<List<ServiceRequest>> {
        return requests.map { list -> list.filter { it.status == RequestStatus.PENDING } }
    }

    override fun getMyRequests(): Flow<List<ServiceRequest>> {
        return requests.map { list ->
            val userId = currentUser?.id ?: return@map emptyList()
            list.filter { it.customerId == userId }
        }
    }

    override fun getMyWorkerRequests(): Flow<List<ServiceRequest>> {
        return requests.map { list ->
            val userId = currentUser?.id ?: return@map emptyList()
            list.filter { it.workerId == userId }
        }
    }

    override fun getRequestById(requestId: String): ServiceRequest? {
        return requests.value.find { it.id == requestId }
    }

    override fun acceptRequest(requestId: String): ServiceRequest? {
        val worker = currentUser ?: return null
        var accepted: ServiceRequest? = null
        requests.update { list ->
            list.map {
                if (it.id == requestId && it.status == RequestStatus.PENDING) {
                    it.copy(
                        status = RequestStatus.ACCEPTED,
                        workerId = worker.id,
                        workerName = worker.name
                    ).also { updated -> accepted = updated }
                } else it
            }
        }
        return accepted
    }

    override fun completeRequest(requestId: String): ServiceRequest? {
        var completed: ServiceRequest? = null
        requests.update { list ->
            list.map {
                if (it.id == requestId && (it.status == RequestStatus.ACCEPTED || it.status == RequestStatus.IN_PROGRESS)) {
                    it.copy(status = RequestStatus.COMPLETED).also { updated -> completed = updated }
                } else it
            }
        }
        return completed
    }

    // ── Rating & Payment ─────────────────────────────────────────────

    override fun submitRating(requestId: String, score: Int, comment: String): ServiceRequest? {
        ratings.add(Rating(requestId, score, comment))
        var rated: ServiceRequest? = null
        requests.update { list ->
            list.map {
                if (it.id == requestId && it.status == RequestStatus.COMPLETED) {
                    it.copy(status = RequestStatus.RATED).also { updated -> rated = updated }
                } else it
            }
        }
        return rated
    }

    override fun makePayment(requestId: String, method: String): ServiceRequest? {
        var paid: ServiceRequest? = null
        requests.update { list ->
            list.map {
                if (it.id == requestId && it.status == RequestStatus.RATED) {
                    it.copy(status = RequestStatus.PAID).also { updated -> paid = updated }
                } else it
            }
        }
        return paid
    }
}

/**
 * Singleton repository instance shared across the app.
 * In production, use Hilt/Koin for DI instead.
 */
object RepositoryProvider {
    val inMemoryRepository: HireMeRepository = InMemoryHireMeRepository()
    var repository: HireMeRepository = com.example.hireme.data.network.ApiHireMeRepository(
        baseUrl = com.example.hireme.data.network.AppConfig.activeBaseUrl,
        fallback = inMemoryRepository
    )
}

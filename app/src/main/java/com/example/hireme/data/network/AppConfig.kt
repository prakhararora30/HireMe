package com.example.hireme.data.network

/**
 * Configuration for the HireMe application backend connection.
 */
object AppConfig {
    /**
     * Live Render Production Cloud Backend URL connected to MongoDB Atlas.
     */
    const val LIVE_RENDER_API_URL = "https://hireme-backend-tdzd.onrender.com/api"

    /**
     * Local Emulator URL (for local debugging).
     */
    const val EMULATOR_API_URL = "http://10.0.2.2:5000/api"

    /**
     * Active API URL used by ApiHireMeRepository.
     */
    var activeBaseUrl: String = LIVE_RENDER_API_URL
}

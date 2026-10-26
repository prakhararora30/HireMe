package com.example.hireme.data.network

import com.example.hireme.data.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ApiHireMeRepositoryTest {

    @Test
    fun login_whenServerUnreachable_throwsServerUnreachableException() {
        // Pointing to a non-existent port on localhost where no server runs
        val repo = ApiHireMeRepository(baseUrl = "http://127.0.0.1:59999/api")

        val exception = assertThrows(ServerUnreachableException::class.java) {
            repo.login("test@example.com", "password123", UserRole.CUSTOMER)
        }

        assertEquals(
            "Cannot connect to server. The server is unreachable at the moment.",
            exception.message
        )
    }

    @Test
    fun register_whenServerUnreachable_throwsServerUnreachableException() {
        val repo = ApiHireMeRepository(baseUrl = "http://127.0.0.1:59999/api")

        val exception = assertThrows(ServerUnreachableException::class.java) {
            repo.register("Test User", "test@example.com", "password123", UserRole.CUSTOMER)
        }

        assertEquals(
            "Cannot connect to server. The server is unreachable at the moment.",
            exception.message
        )
    }
}

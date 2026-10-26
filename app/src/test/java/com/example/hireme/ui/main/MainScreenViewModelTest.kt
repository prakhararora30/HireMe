package com.example.hireme.ui.main

import com.example.hireme.data.InMemoryHireMeRepository
import com.example.hireme.data.UserRole
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Test

class HireMeRepositoryUnitTest {
    @Test
    fun inMemoryRepository_login_returnsUser() {
        val repo = InMemoryHireMeRepository()
        val user = repo.login("test@hireme.com", "pass123", UserRole.CUSTOMER)
        assertNotNull(user)
        assertEquals("test@hireme.com", user.email)
        assertEquals(UserRole.CUSTOMER, user.role)
    }
}

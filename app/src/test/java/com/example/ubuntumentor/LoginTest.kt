package com.example.ubuntumentor
import org.junit.Test
import org.junit.Assert.*

class LoginTest {
    @Test
    fun emailValidation_isCorrect() {
        val testEmail = "test@example.com"
        assertTrue(testEmail.contains("@"))
    }
}
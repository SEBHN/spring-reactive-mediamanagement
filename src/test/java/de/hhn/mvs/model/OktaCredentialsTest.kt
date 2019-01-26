package de.hhn.mvs.model

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class OktaCredentialsTest {

    companion object {
        const val DEFAULT_PASSWORD =  "secret"
    }

    private lateinit var credentials: OktaCredentials


    @Before
    fun setUp() {
        credentials = OktaCredentials()
        credentials.setPassword(DEFAULT_PASSWORD);
    }

    @Test
    fun testSetPasswordList() {
        val passwordMap = mapOf("value" to "myPassword")
        credentials.password = passwordMap
        assertEquals(passwordMap, credentials.password)
    }

    @Test
    fun testHashCode(){
        assertEquals(OktaCredentials(DEFAULT_PASSWORD).hashCode(), credentials.hashCode())
    }
}

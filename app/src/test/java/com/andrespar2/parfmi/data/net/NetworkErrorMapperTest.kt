package com.andrespar2.parfmi.data.net

import com.andrespar2.parfmi.data.model.ProviderError
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NetworkErrorMapperTest {

    @Test
    fun `maps timeouts`() {
        assertEquals(ProviderError.TIMEOUT, NetworkErrorMapper.fromException(SocketTimeoutException()))
    }

    @Test
    fun `maps connectivity problems to no network`() {
        assertEquals(ProviderError.NO_NETWORK, NetworkErrorMapper.fromException(UnknownHostException()))
        assertEquals(ProviderError.NO_NETWORK, NetworkErrorMapper.fromException(ConnectException()))
        assertEquals(ProviderError.NO_NETWORK, NetworkErrorMapper.fromException(IOException()))
    }

    @Test
    fun `maps unknown failures`() {
        assertEquals(ProviderError.UNKNOWN, NetworkErrorMapper.fromException(IllegalStateException()))
    }

    @Test
    fun `maps http status codes`() {
        assertEquals(ProviderError.UNAUTHORIZED, NetworkErrorMapper.fromStatusCode(401))
        assertEquals(ProviderError.UNAUTHORIZED, NetworkErrorMapper.fromStatusCode(403))
        assertEquals(ProviderError.INSUFFICIENT_BALANCE, NetworkErrorMapper.fromStatusCode(402))
        assertEquals(ProviderError.NOT_FOUND, NetworkErrorMapper.fromStatusCode(404))
        assertEquals(ProviderError.TIMEOUT, NetworkErrorMapper.fromStatusCode(408))
        assertEquals(ProviderError.RATE_LIMITED, NetworkErrorMapper.fromStatusCode(429))
        assertEquals(ProviderError.SERVER_ERROR, NetworkErrorMapper.fromStatusCode(500))
        assertEquals(ProviderError.SERVER_ERROR, NetworkErrorMapper.fromStatusCode(503))
        assertEquals(ProviderError.REQUEST_REJECTED, NetworkErrorMapper.fromStatusCode(400))
        assertEquals(ProviderError.UNKNOWN, NetworkErrorMapper.fromStatusCode(200))
    }
}

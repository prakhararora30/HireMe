package com.example.hireme.data.network

import java.io.IOException

/**
 * Thrown when the application cannot connect to the backend server (Render / local / cloud)
 * due to any connection failure, timeout, host resolution error, or 502/503/504 server error.
 */
class ServerUnreachableException(
    message: String = "Cannot connect to server. The server is unreachable at the moment.",
    cause: Throwable? = null
) : IOException(message, cause)

/**
 * Thrown when the server is reachable but the credentials or user request is rejected
 * (e.g., HTTP 400, 401 with invalid credentials, user already exists, etc.).
 */
class AuthException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

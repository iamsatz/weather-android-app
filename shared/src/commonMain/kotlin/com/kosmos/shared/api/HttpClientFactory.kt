package com.kosmos.shared.api

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient

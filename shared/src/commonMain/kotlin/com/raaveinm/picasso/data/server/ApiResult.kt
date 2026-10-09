package com.raaveinm.picasso.data.server

import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

//
// Created by Kirill "Raaveinm" on 10/9/26.
// Copyright (c) 2026 RetrogradeMercury. All rights reserved.
//

/**
 * Statuses carry meaning in the chat contract (403 = "not allowed" for stranger,
 * removed, blocked and unknown alike; 404 = no such thing; 409 = unblock first; 429 =
 * slow down), so the callers - not a generic exception handler - decide what each one
 * means. [Unavailable] is a transport failure: nothing was decided, retrying is safe.
 */
sealed interface ApiResult<out T> {
    data class Ok<T>(val value: T, val status: Int = 200) : ApiResult<T>
    data class Rejected(val status: Int, val message: String?) : ApiResult<Nothing>
    data class Unavailable(val cause: Throwable) : ApiResult<Nothing>
}

/** The server's uniform error body (`ErrorDto`); every field optional because a 404 may be HTML. */
@Serializable
internal data class ErrorBody(val status: Int? = null, val code: String? = null, val message: String? = null)

/** Every REST call carries the session token and nothing else about identity. */
internal fun HttpRequestBuilder.authorize(context: ServerContext) {
    header("Authorization", "Bearer ${context.token}")
}

/**
 * Runs [request], turning transport exceptions into [ApiResult.Unavailable] and
 * non-2xx responses into [ApiResult.Rejected]. Cancellation is never swallowed.
 */
internal suspend inline fun <T> apiCall(
    request: () -> HttpResponse,
    parse: (HttpResponse) -> T
): ApiResult<T> = try {
    val response = request()
    if (response.status.isSuccess()) {
        ApiResult.Ok(parse(response), response.status.value)
    } else {
        val message = runCatching { response.body<ErrorBody>().message }.getOrNull()
        ApiResult.Rejected(response.status.value, message)
    }
} catch (e: Exception) {
    ApiResult.Unavailable(e)
}

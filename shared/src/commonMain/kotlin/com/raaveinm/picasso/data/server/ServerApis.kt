package com.raaveinm.picasso.data.server

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

//
// Created by Kirill "Raaveinm" on 10/9/26.
// Copyright (c) 2026 RetrogradeMercury. All rights reserved.
//

/**
 * Conversations, palette invitations and the history/sync half of chat. Each method is
 * one request; deciding what a status means is the caller's job (see [ApiResult]).
 * Endpoints: brainstorm/chat-sync-contract.md, section 4.
 */
class ConversationsApi(private val http: HttpClient) {

    /** Get-or-create: 201 when new, 200 when the pair already had a dm. Needs mutual allies, else 403. */
    suspend fun createDm(context: ServerContext, peerSteamId: Long): ApiResult<WireConversation> = apiCall(
        request = {
            http.post("${context.baseUrl}/conversations") {
                authorize(context)
                contentType(ContentType.Application.Json)
                setBody(CreateConversationRequest(kind = "dm", peerSteamId = peerSteamId.toString()))
            }
        },
        parse = { it.body() }
    )

    suspend fun createPalette(
        context: ServerContext,
        name: String,
        inviteSteamIds: List<Long>
    ): ApiResult<WireConversation> = apiCall(
        request = {
            http.post("${context.baseUrl}/conversations") {
                authorize(context)
                contentType(ContentType.Application.Json)
                setBody(
                    CreateConversationRequest(
                        kind = "palette",
                        name = name,
                        inviteSteamIds = inviteSteamIds.map { it.toString() }
                    )
                )
            }
        },
        parse = { it.body() }
    )

    suspend fun invite(
        context: ServerContext,
        conversationRemoteId: Long,
        steamId: Long
    ): ApiResult<WireConversation> = apiCall(
        request = {
            http.post("${context.baseUrl}/conversations/$conversationRemoteId/invites") {
                authorize(context)
                contentType(ContentType.Application.Json)
                setBody(SteamIdBody(steamId.toString()))
            }
        },
        parse = { it.body() }
    )

    suspend fun acceptInvite(context: ServerContext, conversationRemoteId: Long): ApiResult<WireConversation> =
        apiCall(
            request = {
                http.post("${context.baseUrl}/palette-invites/$conversationRemoteId/accept") { authorize(context) }
            },
            parse = { it.body() }
        )

    /** Silent on the server: the inviter is never told. Idempotent. */
    suspend fun declineInvite(context: ServerContext, conversationRemoteId: Long): ApiResult<Unit> = apiCall(
        request = { http.delete("${context.baseUrl}/palette-invites/$conversationRemoteId") { authorize(context) } },
        parse = { }
    )

    ///////////////////////////////////////////////
    // Sync and history
    ///////////////////////////////////////////////

    suspend fun sync(context: ServerContext, request: SyncRequest): ApiResult<SyncResponse> = apiCall(
        request = {
            http.post("${context.baseUrl}/sync") {
                authorize(context)
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        },
        parse = { it.body() }
    )

    /** Messages older than [before], ascending, deleted ones excluded. */
    suspend fun history(
        context: ServerContext,
        conversationRemoteId: Long,
        before: Long,
        limit: Int = SYNC_PAGE_SIZE
    ): ApiResult<HistoryPage> = apiCall(
        request = {
            http.get("${context.baseUrl}/conversations/$conversationRemoteId/messages") {
                authorize(context)
                parameter("before", before)
                parameter("limit", limit)
            }
        },
        parse = { it.body() }
    )

    /** Silent delete; sender only, idempotent. */
    suspend fun deleteMessage(
        context: ServerContext,
        conversationRemoteId: Long,
        messageRemoteId: Long
    ): ApiResult<Unit> = apiCall(
        request = {
            http.delete("${context.baseUrl}/conversations/$conversationRemoteId/messages/$messageRemoteId") {
                authorize(context)
            }
        },
        parse = { }
    )
}

class ContactsApi(private val http: HttpClient) {

    suspend fun contacts(context: ServerContext): ApiResult<WireContacts> = apiCall(
        request = { http.get("${context.baseUrl}/contacts") { authorize(context) } },
        parse = { it.body() }
    )

    suspend fun sendRequest(context: ServerContext, steamId: Long): ApiResult<Unit> = apiCall(
        request = {
            http.post("${context.baseUrl}/contacts/requests") {
                authorize(context)
                contentType(ContentType.Application.Json)
                setBody(SteamIdBody(steamId.toString()))
            }
        },
        parse = { }
    )

    suspend fun accept(context: ServerContext, fromSteamId: Long): ApiResult<Unit> = apiCall(
        request = { http.post("${context.baseUrl}/contacts/requests/$fromSteamId/accept") { authorize(context) } },
        parse = { }
    )

    /** As the recipient this declines (silently); as the sender it withdraws. */
    suspend fun declineOrWithdraw(context: ServerContext, steamId: Long): ApiResult<Unit> = apiCall(
        request = { http.delete("${context.baseUrl}/contacts/requests/$steamId") { authorize(context) } },
        parse = { }
    )

    /** "ally" / "friend" on an existing contact, or "imposter" to block. */
    suspend fun setLevel(context: ServerContext, steamId: Long, level: String): ApiResult<Unit> = apiCall(
        request = {
            http.put("${context.baseUrl}/contacts/$steamId") {
                authorize(context)
                contentType(ContentType.Application.Json)
                setBody(LevelBody(level))
            }
        },
        parse = { }
    )

    /** A removal if ally/friend, an unblock if imposter. Idempotent. */
    suspend fun remove(context: ServerContext, steamId: Long): ApiResult<Unit> = apiCall(
        request = { http.delete("${context.baseUrl}/contacts/$steamId") { authorize(context) } },
        parse = { }
    )
}

package com.raaveinm.picasso.data.sync

import com.raaveinm.core.database.dao.AccountDao
import com.raaveinm.core.datastore.auth.UserAuthToken
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

//
// Created by Kirill "Raaveinm" on 10/11/26.
// Copyright (c) 2026 RetrogradeMercury. All rights reserved.
//

/**
 * Keeps one rule: while nobody is signed in, the cache holds nothing about an account
 * (messages, conversations, contacts, friends, libraries - see [AccountDao]).
 *
 * It watches the session instead of being called from logout, so every way of losing
 * the session is covered the same: the user signing out, the server rejecting the
 * token ([SyncCoordinator.sessionRejected]), and an app that was killed between
 * clearing the token and wiping - the next start finds no session and wipes then.
 *
 * The wipe waits for [SyncCoordinator.awaitIdle]: a sync that was mid-write when the
 * session went away would otherwise put rows back after the wipe.
 */
class SignedOutCacheGuard(
    private val session: Flow<UserAuthToken?>,
    private val syncCoordinator: SyncCoordinator,
    private val accountDao: AccountDao,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private var job: Job? = null

    /** Idempotent. Called once from the app shell, next to [SyncCoordinator.start]. */
    fun start() {
        if (job != null) return
        job = scope.launch {
            session
                .map { it == null }
                .distinctUntilChanged()
                // collect, not collectLatest: a quick re-login must not cancel the wipe
                // and leave the previous account's data to the next one.
                .collect { signedOut -> if (signedOut) wipe() }
        }
    }

    private suspend fun wipe() {
        try {
            syncCoordinator.awaitIdle()
            accountDao.wipeAccountData()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Retried on the next start that finds no session.
            println("SignedOutCacheGuard: wiping account data failed: $e")
        }
    }
}

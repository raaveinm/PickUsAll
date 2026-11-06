package com.raaveinm.picasso.ui.app.screens

//
// Created by Kirill "Raaveinm" on 11/05/2026.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

private const val STORE_SCROLL_MESSAGE_PREFIX: String = "store-scroll:"

/** Positive deltas move down the list. The page reports CSS pixels, converted by its caller. */
internal fun storeDescriptionScrollDelta(message: String): Float? {
    if (!message.startsWith(STORE_SCROLL_MESSAGE_PREFIX)) return null
    return message.removePrefix(STORE_SCROLL_MESSAGE_PREFIX).toFloatOrNull()?.takeIf { it.isFinite() }
}

// Only outward movement is sent to Compose. Inward movement and nested HTML scrollers stay native.
internal val STORE_DESCRIPTION_SCROLL_SCRIPT: String = """
    (function () {
        const root = document.scrollingElement;
        if (!root) return;

        let pendingDelta = 0;
        let pendingFrame = null;

        function flush() {
            const delta = pendingDelta;
            pendingDelta = 0;
            pendingFrame = null;
            if (delta === 0) return;
            const message = '$STORE_SCROLL_MESSAGE_PREFIX' + delta;
            if (window.PicassoWebView) {
                window.PicassoWebView.postMessage(message);
            } else if (window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.picasso) {
                window.webkit.messageHandlers.picasso.postMessage(message);
            } else if (window.cefQuery) {
                window.cefQuery({ request: 'picasso-message:' + message });
            }
        }

        function send(delta) {
            if (!Number.isFinite(delta) || delta === 0) return;
            pendingDelta += delta;
            if (pendingFrame === null) pendingFrame = requestAnimationFrame(flush);
        }

        function nestedCanScroll(target, delta) {
            for (let node = target; node && node !== root; node = node.parentElement) {
                if (node.nodeType !== 1) continue;
                const overflow = getComputedStyle(node).overflowY;
                if (!/^(auto|scroll|overlay)$/.test(overflow)) continue;
                const maximum = node.scrollHeight - node.clientHeight;
                if (delta < 0 ? node.scrollTop > 0 : node.scrollTop < maximum - 1) return true;
            }
            return false;
        }

        function handOff(event, delta) {
            if (!Number.isFinite(delta) || delta === 0) return;
            const maximum = Math.max(0, root.scrollHeight - root.clientHeight);
            const position = Math.max(0, Math.min(maximum, root.scrollTop));
            // Observe the edge without canceling a wheel/touch sequence or its native momentum.
            // A movement that reaches the edge stays native; the next outward movement is handed off.
            const atEdge = delta < 0 ? position <= 0 : position >= maximum;
            if (!atEdge || nestedCanScroll(event.target, delta)) return;
            send(delta);
        }

        document.addEventListener('wheel', function (event) {
            if (event.ctrlKey || event.shiftKey || Math.abs(event.deltaX) > Math.abs(event.deltaY)) return;
            const unit = event.deltaMode === 1
                ? parseFloat(getComputedStyle(root).lineHeight) || 16
                : event.deltaMode === 2 ? root.clientHeight : 1;
            handOff(event, event.deltaY * unit);
        }, { passive: true });

        let touchX = null;
        let touchY = null;
        document.addEventListener('touchstart', function (event) {
            touchX = event.touches.length === 1 ? event.touches[0].clientX : null;
            touchY = event.touches.length === 1 ? event.touches[0].clientY : null;
        }, { passive: true });
        document.addEventListener('touchmove', function (event) {
            if (touchY === null || event.touches.length !== 1) {
                touchX = null;
                touchY = null;
                return;
            }
            const touch = event.touches[0];
            const deltaX = touchX - touch.clientX;
            const deltaY = touchY - touch.clientY;
            touchX = touch.clientX;
            touchY = touch.clientY;
            if (Math.abs(deltaY) >= Math.abs(deltaX)) handOff(event, deltaY);
        }, { passive: true });
        function resetTouch() { touchX = null; touchY = null; }
        document.addEventListener('touchend', resetTouch, { passive: true });
        document.addEventListener('touchcancel', resetTouch, { passive: true });
    })();
""".trimIndent()

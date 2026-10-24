package com.raaveinm.picasso.ui.actions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import com.raaveinm.pickusall.core.designsystem.components.GameCard
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalComposeUiApi::class)
class SideActionGestureTest {
    private class Counts { var click = 0; var side = 0; var double = 0 }

    private fun run(insideGameCard: Boolean, gesture: (ImageComposeScene) -> Unit): Counts {
        val counts = Counts()
        val scene = ImageComposeScene(width = 200, height = 300, density = Density(1f)) {
            val modifier = Modifier.sideAction(
                onSideAction = { counts.side++ },
                onDoubleClick = { counts.double++ },
                onClick = { counts.click++ },
            )
            if (insideGameCard) {
                GameCard(modifier = modifier, gameId = 1, text = null, enableGlowEffect = false)
            } else {
                Box(modifier = modifier.aspectRatio(2f / 3f).background(Color.Red))
            }
        }
        scene.render(0L)
        gesture(scene)
        scene.close()
        return counts
    }

    private fun ImageComposeScene.press(button: PointerButton = PointerButton.Primary) =
        sendPointerEvent(PointerEventType.Press, Offset(50f, 50f), button = button)

    private fun ImageComposeScene.release(button: PointerButton = PointerButton.Primary) =
        sendPointerEvent(PointerEventType.Release, Offset(50f, 50f), button = button)

    private fun ImageComposeScene.tap() { press(); release() }

    private fun ImageComposeScene.wait(ms: Long) {
        val end = System.currentTimeMillis() + ms
        while (System.currentTimeMillis() < end) { render(System.nanoTime()); Thread.sleep(10) }
    }

    @Test fun clickPlain() = assertEquals(1, run(false) { it.tap(); it.wait(500) }.click)
    @Test fun clickInGameCard() = assertEquals(1, run(true) { it.tap(); it.wait(500) }.click)

    @Test fun longPressPlain() = assertEquals(1, run(false) { it.press(); it.wait(800); it.release() }.side)
    @Test fun longPressInGameCard() = assertEquals(1, run(true) { it.press(); it.wait(800); it.release() }.side)

    @Test fun doubleClickPlain() = assertEquals(1, run(false) { it.tap(); it.wait(60); it.tap(); it.wait(500) }.double)
    @Test fun doubleClickInGameCard() = assertEquals(1, run(true) { it.tap(); it.wait(60); it.tap(); it.wait(500) }.double)

    @Test fun rightClickPlain() = assertEquals(1, run(false) {
        it.press(PointerButton.Secondary); it.release(PointerButton.Secondary)
    }.side)

    @Test fun rightClickInGameCard() = assertEquals(1, run(true) {
        it.press(PointerButton.Secondary); it.release(PointerButton.Secondary)
    }.side)
}

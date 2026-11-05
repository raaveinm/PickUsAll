package org.cef.browser;

import org.cef.CefClient;
import org.cef.callback.CefDragData;
import org.cef.handler.CefRenderHandler;
import org.cef.handler.CefScreenInfo;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.swing.JPanel;

/**
 * An alpha-preserving CEF surface painted by Compose rather than a native window.
 * This adapter lives in CEF's package because its native browser base is package-private.
 */
public final class ComposeCefBrowser extends CefBrowser_N implements CefRenderHandler {
    private static final double PIXELS_PER_SCROLL_UNIT = 16.0;
    private final JPanel eventSource = new JPanel();
    private final Consumer<BufferedImage> onFrame;
    private volatile Rectangle viewport = new Rectangle(0, 0, 1, 1);
    private volatile Point screenOrigin = new Point();
    private volatile double scale = 1.0;
    private volatile BufferedImage image;
    private double scrollRemainder = 0.0;

    public ComposeCefBrowser(
        CefClient client,
        String url,
        CefRequestContext context,
        Consumer<BufferedImage> onFrame
    ) {
        super(client, url, context, null, null, null);
        this.onFrame = onFrame;
    }

    @Override
    public void createImmediately() {
        createBrowser(getClient(), 0L, getUrl(), true, true, null, getRequestContext());
    }

    public void resize(int width, int height, double scale, int screenX, int screenY) {
        this.scale = scale;
        this.screenOrigin = new Point(screenX, screenY);
        this.viewport = new Rectangle(
            0, 0, Math.max(1, (int) Math.ceil((double) width / scale)),
            Math.max(1, (int) Math.ceil((double) height / scale))
        );
        wasResized(viewport.width, viewport.height);
    }

    public void pointerEvent(MouseEvent original, int x, int y) {
        if (original instanceof MouseWheelEvent) {
            MouseWheelEvent wheel = (MouseWheelEvent) original;
            // AWT and CEF use opposite wheel directions. Retain fractional trackpad movement;
            // JCEF's native bridge reads integer rotations, so send an accumulated pixel delta.
            double unitSize = wheel.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL
                ? (double) viewport.height : (double) wheel.getScrollAmount() * PIXELS_PER_SCROLL_UNIT;
            double delta = scrollRemainder - wheel.getPreciseWheelRotation() * unitSize;
            int pixels = (int) delta;
            scrollRemainder = delta - (double) pixels;
            if (pixels == 0) return;
            sendMouseWheelEvent(new MouseWheelEvent(
                eventSource, wheel.getID(), wheel.getWhen(), wheel.getModifiersEx(), x, y,
                x + screenOrigin.x, y + screenOrigin.y, wheel.getClickCount(), wheel.isPopupTrigger(),
                MouseWheelEvent.WHEEL_BLOCK_SCROLL, 1, pixels, (double) pixels
            ));
        } else {
            sendMouseEvent(new MouseEvent(
                eventSource, original.getID(), original.getWhen(), original.getModifiersEx(), x, y,
                original.getClickCount(), original.isPopupTrigger(), original.getButton()
            ));
        }
    }

    public void keyEvent(KeyEvent event) {
        sendKeyEvent(event);
    }

    @Override
    public Component getUIComponent() {
        return eventSource;
    }

    @Override
    public CefRenderHandler getRenderHandler() {
        return this;
    }

    @Override
    protected CefBrowser_N createDevToolsBrowser(
        CefClient client, String url, CefRequestContext context, CefBrowser_N parent, Point inspectAt
    ) {
        throw new UnsupportedOperationException("DevTools windows require the native browser surface.");
    }

    @Override
    public Rectangle getViewRect(CefBrowser browser) {
        return new Rectangle(viewport);
    }

    @Override
    public boolean getScreenInfo(CefBrowser browser, CefScreenInfo screenInfo) {
        screenInfo.Set(scale, 32, 8, false, new Rectangle(viewport), new Rectangle(viewport));
        return true;
    }

    @Override
    public Point getScreenPoint(CefBrowser browser, Point point) {
        return new Point(screenOrigin.x + point.x, screenOrigin.y + point.y);
    }

    @Override
    public void onPaint(
        CefBrowser browser, boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height
    ) {
        if (popup || width <= 0 || height <= 0) return;
        // CEF supplies premultiplied BGRA pixels; preserve their alpha when copying out of native memory.
        BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        int[] pixels = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();
        buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(pixels);
        image = frame;
        onFrame.accept(frame);
    }

    @Override
    public CompletableFuture<BufferedImage> createScreenshot(boolean nativeResolution) {
        return CompletableFuture.completedFuture(image);
    }

    @Override public void onPopupShow(CefBrowser browser, boolean show) {}
    @Override public void onPopupSize(CefBrowser browser, Rectangle size) {}
    @Override public void addOnPaintListener(Consumer<CefPaintEvent> listener) {}
    @Override public void setOnPaintListener(Consumer<CefPaintEvent> listener) {}
    @Override public void removeOnPaintListener(Consumer<CefPaintEvent> listener) {}
    @Override public boolean onCursorChange(CefBrowser browser, int cursorType) { return false; }
    @Override public boolean startDragging(CefBrowser browser, CefDragData data, int mask, int x, int y) { return false; }
    @Override public void updateDragCursor(CefBrowser browser, int operation) {}
}

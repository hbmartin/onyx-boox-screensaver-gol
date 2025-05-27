package me.haroldmartin.golwallpaper

import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import me.haroldmartin.golwallpaper.domain.GolController
import me.haroldmartin.golwallpaper.utils.BitmapRenderer

class GolEngine : WallpaperService.Engine() {

    private lateinit var golController: GolController
    private lateinit var bitmapRenderer: BitmapRenderer

    private val handler = Handler(Looper.getMainLooper())
    private var visible = false
    private val drawRunner = Runnable { draw() }

    override fun onCreate(surfaceHolder: SurfaceHolder) {
        super.onCreate(surfaceHolder)
        bitmapRenderer = BitmapRenderer(
            context = applicationContext,
            backgroundColor = android.graphics.Color.BLACK,
            cellColor = android.graphics.Color.WHITE
        )
        golController = GolController()
    }

    override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        super.onSurfaceChanged(holder, format, width, height)
        golController.setSize(width, height)
        bitmapRenderer.recreateBitmap(width, height)
    }

    override fun onVisibilityChanged(visible: Boolean) {
        this.visible = visible
        if (visible) {
            handler.post(drawRunner)
        } else {
            handler.removeCallbacks(drawRunner)
        }
    }

    override fun onSurfaceDestroyed(holder: SurfaceHolder) {
        super.onSurfaceDestroyed(holder)
        visible = false
        handler.removeCallbacks(drawRunner)
    }

    private fun draw() {
        var canvas: Canvas? = null
        try {
            canvas = surfaceHolder.lockCanvas()
            if (canvas != null) {
                golController.tick()
                val bitmap = bitmapRenderer.render(golController.gol)
                canvas.drawBitmap(bitmap, 0f, 0f, null)
            }
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
        }
        // Schedule next draw
        handler.removeCallbacks(drawRunner)
        if (visible) {
            handler.postDelayed(drawRunner, 50L) // Adjust frame rate as needed
        }
    }
}

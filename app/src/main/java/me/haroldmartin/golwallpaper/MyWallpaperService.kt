package me.haroldmartin.golwallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import me.haroldmartin.golwallpaper.domain.GolController
import me.haroldmartin.golwallpaper.domain.Patterns
import me.haroldmartin.golwallpaper.utils.createBitmapFromBooleanArray

class MyWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return GolEngine()
    }

    inner class GolEngine : WallpaperService.Engine() {
        // All the logic previously in GolEngine.kt (fields, onCreate, onSurfaceChanged, etc.)
        // is now here.

        private lateinit var golController: GolController

        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private val drawRunner = Runnable { draw() }

        private var surfaceWidth: Int = 1
        private var surfaceHeight: Int = 1

        private val backgroundColor = Color.BLACK
        private val cellColor = Color.WHITE
        private val cellPixelSize = 20 // Default cell size in pixels

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            // GolController initialization is deferred to onSurfaceChanged
            // where dimensions are known.
            // If context from MyWallpaperService is needed in the future,
            // it can be accessed using this@MyWallpaperService.applicationContext
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            surfaceWidth = if (width > 0) width else 1
            surfaceHeight = if (height > 0) height else 1

            val rows = surfaceHeight / cellPixelSize
            val cols = surfaceWidth / cellPixelSize

            // Initialize or re-initialize GolController with new dimensions
            if (rows > 0 && cols > 0) {
                golController = GolController(
                    rows = rows,
                    columns = cols,
                    initialPattern = Patterns.HERRINGBONE_AGAR_P14.value, // Default pattern
                    rule = "B3/S23" // Standard Game of Life rule
                )
            }

            // If wallpaper is already visible, start drawing.
            // This also handles cases where the surface is created after visibility is true.
            if (visible && ::golController.isInitialized) {
                handler.post(drawRunner)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                // Only schedule draw if controller is initialized (surface is ready)
                // and surface dimensions are valid.
                if (::golController.isInitialized && surfaceWidth > 0 && surfaceHeight > 0) {
                    handler.post(drawRunner)
                }
            } else {
                handler.removeCallbacks(drawRunner)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawRunner)
            // Consider any other cleanup if necessary
        }

        private fun draw() {
            // Don't draw if controller isn't initialized or surface is not valid
            if (!::golController.isInitialized || surfaceWidth <= 0 || surfaceHeight <= 0) {
                return
            }

            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    golController.update()
                    val bitmap = createBitmapFromBooleanArray(
                        width = surfaceWidth,
                        height = surfaceHeight,
                        trueColor = cellColor,
                        falseColor = backgroundColor,
                        grid = golController.grid
                    )
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
                handler.postDelayed(drawRunner, 50L) // ~20 FPS
            }
        }
    }
}

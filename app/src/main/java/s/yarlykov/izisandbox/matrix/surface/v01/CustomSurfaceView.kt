package s.yarlykov.izisandbox.matrix.surface.v01

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import s.yarlykov.izisandbox.matrix.surface.Monitor
import s.yarlykov.izisandbox.matrix.surface.Renderer
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.max

class CustomSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback, Renderer {

    private var surfaceHeight = 0f
    private var surfaceWidth = 0f

    private var threadDrawing: DrawingThread? = null
    private var monitor: Monitor? = null

    private var radius = 0f

    /**
     * Paint (color Red)
     */
    private val paintCircle: Paint = Paint().apply {
        color = Color.argb(0xff, 0xff, 0x00, 0x0)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    init {
        holder.apply { addCallback(this@CustomSurfaceView) }
    }

    fun resume() {
        monitor = Monitor().also {
            threadDrawing = DrawingThread(it, this).apply { startThread() }
        }
    }

    fun pause() {
        try {
            threadDrawing?.run {
                stopThread()
                monitor?.wakeUpSleeping()
                join()
            }
        } catch (e: InterruptedException) {
            logIt("pause() InterruptedException caught", true)
        }
    }

    override fun render(point: PointF) {
        if (holder.surface.isValid) {

            holder.lockCanvas()?.let { canvas ->
                try {
                    canvas.save()
                    canvas.drawColor(Color.WHITE)
                    canvas.drawCircle(point.x, point.y, radius, paintCircle)
                    canvas.restore()
                } catch (e: Exception) {
                } finally {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                monitor?.point = PointF(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                monitor?.point = PointF(event.x, event.y)
            }
            MotionEvent.ACTION_UP -> {
            }
        }
        return true
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        surfaceHeight = height.toFloat()
        surfaceWidth = width.toFloat()

        radius = max(surfaceWidth, surfaceHeight) / 10f
        monitor?.point = PointF(surfaceWidth / 2f, surfaceHeight / 2f)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    override fun surfaceCreated(holder: SurfaceHolder) {}
}
package s.yarlykov.izisandbox.matrix.surface.v02

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import s.yarlykov.izisandbox.matrix.surface.Monitor
import s.yarlykov.izisandbox.matrix.surface.Renderer
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.max

class SmoothSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback, Renderer {

    companion object {
        const val CONTOUR_WIDTH = 2f
        const val CORNER_RADIUS = 10f
    }

    private var surfaceHeight = 0f
    private var surfaceWidth = 0f

    private var threadDrawing: SmoothDrawingThread? = null
    private var monitor: Monitor? = null

    private var radius = 0f

    private var cacheBitmap: Bitmap? = null
    private var cacheCanvas: Canvas? = null

    private val identityMatrix = Matrix()
    private val square = RectF()

    /**
     * При работе с этим классом показалось, что плавность движения "заикается"
     */
    private val colorGenerator = ArgbEvaluator()

    /**
     * Paint (color Red)
     */
    private val paintRed255: Paint = Paint().apply {
        color = Color.argb(0xff, 0xff, 0x00, 0x0)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    /**
     * Red бледнее
     */
    private val paintRed121: Paint = Paint().apply {
        color = Color.argb(0xff, 0xff, 0x79, 0x79)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        strokeWidth = CONTOUR_WIDTH
    }

    /**
     * Red совсем бледный. Светло светло розовый.
     */
    private val paintRed215: Paint = Paint().apply {
        color = Color.argb(0xff, 0xff, 0xd7, 0xd7)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        strokeWidth = CONTOUR_WIDTH
    }

    /**
     * Paint (color Blue)
     */
    private val paintSquare: Paint = Paint().apply {
        color = Color.argb(0xff, 0x00, 0x00, 0xff)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        strokeWidth = 2f
    }

    init {
        holder.apply { addCallback(this@SmoothSurfaceView) }
    }

    fun resume() {
        monitor = Monitor().also {
            threadDrawing = SmoothDrawingThread(it, this).apply { startThread() }
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
            logIt("InterruptedException caught", true)
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

    override fun render(point: PointF) {

        if (holder.surface.isValid) {

            holder.lockCanvas()?.let { canvas ->
                try {
                    cacheCanvas?.let { c ->
                        c.drawColor(Color.WHITE)
                        drawSpot(point, c, paintRed255)
                        drawCircle(point, radius + CONTOUR_WIDTH * 0.5f, c, paintRed121)
                        drawCircle(point, radius + CONTOUR_WIDTH * 1.5f, c, paintRed215)
                        canvas.drawBitmap(cacheBitmap!!, identityMatrix, null)
                    }

                } catch (e: Exception) {
                } finally {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }
    }

    /**
     * fraction = 0f, получим Color.RED
     * fraction = 1f, получим Color.WHITE
     */
    private fun tintRed(fraction : Float) : Int {
        return colorGenerator.evaluate(fraction, Color.RED, Color.WHITE) as Int
    }

    private fun drawSpot(point: PointF, canvas: Canvas, paint : Paint) {
        canvas.drawCircle(point.x, point.y, radius, paint)
    }

    private fun drawCircle(point: PointF, radius : Float, canvas: Canvas, paint : Paint) {
        canvas.drawCircle(point.x, point.y, radius, paint)
    }

    private fun drawSquare(point: PointF, canvas: Canvas) {
        square.apply {
            left = point.x - radius
            right = point.x + radius
            top = point.y - radius
            bottom = point.y + radius
        }

        canvas.drawRoundRect(square, CORNER_RADIUS, CORNER_RADIUS, paintSquare)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        surfaceHeight = height.toFloat()
        surfaceWidth = width.toFloat()

        radius = max(surfaceWidth, surfaceHeight) / 10f
        monitor?.point = PointF(surfaceWidth / 2f, surfaceHeight / 2f)

        cacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
            cacheCanvas = Canvas().apply { setBitmap(it) }
        }

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        cacheBitmap?.apply { recycle() }
    }
}
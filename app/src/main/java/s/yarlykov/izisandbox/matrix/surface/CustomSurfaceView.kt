package s.yarlykov.izisandbox.matrix.surface

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.max
import kotlin.math.min

class CustomSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback, Runnable {

    private var surfaceHeight = 0f
    private var surfaceWidth = 0f

    private var threadDrawing: DrawingThread? = null
    private var thread: Thread? = null
//    private var monitor: Monitor? = null

    private var lastX = 0f
    private var lastY = 0f

    private var radius = 0f

    init {
        holder.apply { addCallback(this@CustomSurfaceView) }
    }

    /**
     * Paint (color Red)
     */
    private val paintCircle: Paint = Paint().apply {
        color = Color.argb(0xff, 0xff, 0x00, 0x0)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var isValueSet = false
    private val lock = Object()

    private val point = PointF(0f, 0f)

    private fun getPoint(): PointF {
        synchronized(lock) {

            while (!isValueSet) {
                try {
                    lock.wait()
                } catch (e: InterruptedException) {
                    logIt("InterruptedException caught")
                }
            }
            logIt("CustomSurfaceView::getPoint OK, point=$point", true, "PLPL")
            isValueSet = false
            lock.notify()
            return point
        }
    }

    private fun setPoint(x : Float, y : Float) {
        synchronized(lock) {
            while (isValueSet) {
                try {
                    lock.wait()
                } catch (e: InterruptedException) {
                    logIt("InterruptedException caught")
                }
            }
            logIt("CustomSurfaceView::setPoint OK", true, "PLPL")
            point.x = x
            point.y = y
            lock.notify()
            isValueSet = true
        }
    }

    fun resume() {
//        logIt("CustomSurfaceView::resume OK", true, "PLPL")
//        isRunning = true
//        thread = Thread(this).apply { start() }
    }

    fun pause() {
        setPoint(0f, 0f)
        isRunning = false
        logIt("CustomSurfaceView::pause OK", true, "PLPL")

        try {
            thread?.join()
        } catch (e: InterruptedException) {
            logIt("pause() InterruptedException caught", true, "PLPL")
        }
    }

    private var isRunning = false

    override fun run() {

        logIt(
            "CustomSurfaceView::run() started",
            true,
            "PLPLPL"
        )

        while (isRunning) {

            getPoint().also { center ->

                if(!isRunning) return@also

                if (holder.surface.isValid) {
                    logIt(
                        "CustomSurfaceView::render center=$center, radius=$radius, thread=${Thread.currentThread().name}",
                        true,
                        "PLPLPL"
                    )

                    val canvas = holder.lockCanvas()

                    canvas.save()
                    canvas.drawColor(Color.WHITE)
                    canvas.drawCircle(center.x, center.y, radius, paintCircle)
                    canvas.restore()

                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }
    }

//    fun render() {
//        monitor?.getPoint()?.let { center ->
//            logIt(
//                "CustomSurfaceView::render center=$center, radius=$radius, thread=${Thread.currentThread().name}",
//                true,
//                "PLPLPL"
//            )
//
//            val canvas = holder.lockCanvas()
//
//            canvas.save()
//            canvas.drawColor(Color.WHITE)
//            canvas.drawCircle(center.x, center.y, radius, paintCircle)
//            canvas.restore()
//            holder.unlockCanvasAndPost(canvas)
//        }
//    }

    private fun startDrawing() {
//        logIt(
//            "CustomSurfaceView::startDrawing, thread=${Thread.currentThread().name}",
//            true,
//            "PLPLPL"
//        )
//
//
//        monitor = Monitor().apply {
//            setPoint(PointF(width / 2f, height / 2f))
//        }
//        threadDrawing = DrawingThread(this@CustomSurfaceView).apply {
//            startThread()
//        }
    }

    private fun stopDrawing() {
//        logIt(
//            "CustomSurfaceView::stopDrawing, thread=${Thread.currentThread().name}",
//            true,
//            "PLPLPL"
//        )
//        threadDrawing?.stopThread()
//        monitor = null
//        threadDrawing = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                logIt("ACTION_DOWN x,y = ${event.x},${event.y}", true, "PLPL")
                setPoint(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                logIt("ACTION_MOVE x,y = ${event.x},${event.y}", true, "PLPL")
                setPoint(event.x, event.y)
            }
            MotionEvent.ACTION_UP -> {
            }
        }

        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        logIt(
            "CustomSurfaceView::onSizeChanged WxH=${w}x${h}, thread=${Thread.currentThread().name}",
            true,
            "PLPLPL"
        )
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        logIt(
            "CustomSurfaceView::surfaceChanged WxH=${width}x${height}, thread=${Thread.currentThread().name}",
            true,
            "PLPLPL"
        )

        surfaceHeight = height.toFloat()
        surfaceWidth = width.toFloat()

        radius = max(surfaceWidth, surfaceHeight) / 10f

        setPoint(surfaceWidth/2f, surfaceHeight/2f)

        logIt("CustomSurfaceView::resume OK", true, "PLPL")
        isRunning = true
        thread = Thread(this).apply { start() }


//        surfaceHeight = height.toFloat()
//        surfaceWidth = width.toFloat()
//        radius = min(surfaceWidth, surfaceHeight) / 10f
//
//        monitor?.center = PointF(surfaceWidth/2, surfaceHeight/2)


//        threadDrawing?.let {
//            monitor?.setPoint(PointF(0f, 0f))
//            it.stopThread()
//        }
//
//        threadDrawing = null

//        startDrawing()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        logIt(
            "CustomSurfaceView::surfaceDestroyed, thread=${Thread.currentThread().name}",
            true,
            "PLPLPL"
        )
//        stopDrawing()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        logIt(
            "CustomSurfaceView::surfaceCreated, thread=${Thread.currentThread().name}",
            true,
            "PLPLPL"
        )
//        startDrawing()
    }
}
package s.yarlykov.izisandbox.matrix.surface

import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class CustomSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    private var surfaceHeight = 0
    private var surfaceWidth = 0

    private var thread : DrawingThread? = null
    private var monitor : Monitor? = null

    private var lastX = 0f
    private var lastY = 0f

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    private fun startDrawing(holder: SurfaceHolder) {
        if(thread == null) {
            monitor = Monitor().also {
                thread = DrawingThread(this@CustomSurfaceView, it).apply {
                    startThread()
                }
            }
        }
    }

    private fun stopDrawing() {
        thread?.stopThread()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                monitor?.center = PointF(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                monitor?.center = PointF(event.x, event.y)
            }
            MotionEvent.ACTION_UP ->{

            }
        }

        return true
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        surfaceHeight = height
        surfaceWidth = width
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopDrawing()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        startDrawing()
    }
}
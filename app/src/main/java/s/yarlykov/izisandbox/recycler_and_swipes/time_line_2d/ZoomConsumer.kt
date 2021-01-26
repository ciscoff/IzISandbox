package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d

interface ZoomConsumer {
    fun onZoomBegin(initZoom : Float)
    fun onZoomChanged(zoom : Float)
    fun onZoomEnd()
    val zoomMin : Float
    val zoomMax : Float
}
package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d

interface ZoomConsumer {
    fun onZoomBegin()
    fun onZoomChanged(zoom : Float)
    fun onZoomEnd()
}
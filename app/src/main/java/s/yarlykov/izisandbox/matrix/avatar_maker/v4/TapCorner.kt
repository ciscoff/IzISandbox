package s.yarlykov.izisandbox.matrix.avatar_maker.v4

import s.yarlykov.izisandbox.matrix.avatar_maker.TapArea
import kotlin.math.abs

data class TapCorner(val tapArea: TapArea, val tapX : Float, val cornerX: Float) {
    private var prevDist = abs(tapX - cornerX)

}
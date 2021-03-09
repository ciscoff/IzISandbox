package s.yarlykov.izisandbox.matrix

import s.yarlykov.izisandbox.matrix.avatar_maker.BitmapViewRelation

data class BitmapParams(
    val relation: BitmapViewRelation,
    val width: Int,
    val height: Int,
    var viewPortWidth: Int,
    var viewPortHeight: Int
)
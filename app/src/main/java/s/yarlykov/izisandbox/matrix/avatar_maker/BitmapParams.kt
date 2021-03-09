package s.yarlykov.izisandbox.matrix.avatar_maker

/**
 * @param relation - ориентация битмапы относительно ориентации View
 * @param width - оригинальная ширина битмапы
 * @param height - оригинальная высота битмапы
 *
 * @param viewPortWidth
 * @param viewPortHeight - Размеры области на экране (дочерних views), внутри которой
 * будем показывать bitmap.
 */

data class BitmapParams(
    val relation: BitmapViewRelation,
    val width: Int,
    val height: Int,
    val viewPortWidth: Int,
    val viewPortHeight: Int
)
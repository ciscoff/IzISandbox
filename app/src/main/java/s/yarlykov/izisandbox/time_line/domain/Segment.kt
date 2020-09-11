package s.yarlykov.izisandbox.time_line.domain

import s.yarlykov.izisandbox.extensions.roundTo
import kotlin.math.abs
import kotlin.math.min

class Segment(val x1: Int, val x2: Int) {
    private val range = x1 until x2

    val center = (x2 + x1) / 2

    val length: Int
        get() = x2 - x1

    operator fun component1() = x1
    operator fun component2() = x2

    // Пересечение.
    fun intersect(other: Segment): Boolean {
        return other.x1 in range || other.x2 in range
    }

    // Текущий накрывает другого
    fun overlapping(other: Segment): Boolean {
        return other.x1 in range && other.x2 in range
    }

    // Другой накрывает текущего
    fun overlapped(other: Segment): Boolean {
        return x1 in other.range && x2 in other.range
    }

    // Ищем ближайшего соседа по расстоянию от своего центра до ближейшей
    // стороны соседа.
    fun neighbor(list: List<Segment>): Segment? {

        var neighbor: Segment? = null
        var minDistance: Int = Int.MAX_VALUE

        list.forEach { s ->
            val (ox1, ox2) = s

            val d = min(abs(center - ox1), abs(center - ox2))

            if (d < minDistance) {
                minDistance = d
                neighbor = s
            }
        }

        return neighbor
    }

    /**
     * Разделить данный сегмент на подсегменты заданной длины.
     */
    fun split(size: Int): List<Segment> {
        var chunks = emptyList<Segment>()

        if (size <= length) {
            chunks = range.chunked(size).map { Segment(it.first(), it.last()) }
        }

        return chunks
    }

    /**
     * Разделяет исходный диапазон, на перекрывающиеся поддиапазоны с шагом step
     * Получаем (с шагом 1):
     *    *****
     *     +++++
     *      -----
     */

    fun splitOverlapped(size: Int, step: Int): List<Segment> {
        val result = mutableListOf<Segment>()

        if (size <= length) {
            result += Segment(x1, x1 + size)
            result += Segment(x1 + step, x2).splitOverlapped(size, step)
        }

        return result
    }

    /**
     * Создать новый сегмент с нормализованной длиной и тем же началом.
     * Длина нормализована если кратна слоту.
     */
    fun normalize(arg: Int, threshold: Int): Segment {
        var l = length.roundTo(arg)

        while (l > threshold) {
            l -= arg
        }

        return Segment(x1, x1 + l)
    }
}

fun Segment.toPx(factor: Float) = (x1 * factor).toInt() to (x2 * factor).toInt()

/**
 * Функция находит сегмент, который больше перекрывается с исходным.
 * На картинке ниже это будет Other2. У него общая протяженность с
 * This больше чем у Other1.
 */

// ************************************************
//
//  Other1:  |---------|
//  Other2:       |---------|
//  This:        |=========|
//
//  Other2 is winner !
//
// ************************************************

fun Segment.findCloser(list: List<Segment>): Segment? {

    var segment: Segment? = null
    var maxOverlap = 0

    list.filter { other ->
        this.intersect(other)
    }.forEach { other ->
        val (ox1, ox2) = other
        val d = min(abs(x1 - ox2), abs(ox1 - x2))

        if (d > maxOverlap) {
            maxOverlap = d
            segment = other
        }
    }

    return segment
}
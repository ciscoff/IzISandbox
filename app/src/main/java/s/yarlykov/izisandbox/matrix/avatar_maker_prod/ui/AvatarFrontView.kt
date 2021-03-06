package s.yarlykov.izisandbox.matrix.avatar_maker_prod.ui

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.center
import s.yarlykov.izisandbox.extensions.scale
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.gesture.*
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.media.MediaData
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sign
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AvatarFrontView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AvatarBaseView(context, attrs, defStyleAttr) {

    private var mode: Mode = Mode.Waiting

    /**
     * @rectClip - квадратная область для рисования рамки выбора части изображения под аватарку.
     * Это квадрат в координатах канвы/view.
     */
    private var rectClip: RectF by rectClipDelegate()

    /**
     * @rectAnchor - квадрат в координатах канвы, который играет роль хранилилища предыдущего
     * положения rectClip в операции Dragging: он помнит где был rectClip в момент предыдущего
     * ACTION_DOWN и при очередном ACTION_MOVE rectClip сначала позиционируется на rectAnchor,
     * а потом смещается в новое положение.
     */
    private val rectAnchor = RectF()

    /**
     * @rectClipShifted служит для временного копирования rectClip в операциях скалирования
     */
    private val rectClipShifted = RectF()

    /**
     * @rectBorder прямоугольник для рамки
     */
    private val rectBorder: RectF by rectBorderDelegate()

    /**
     * Содержит минимально допустимый размер для viewPort'а в пассивном состоянии.
     * То есть это квадрат со стороной равной 3/5 от наименьшей стороны rectVisible.
     */
    private val rectMin: RectF by rectMinDelegate()

    /**
     * @pathClip определяется через @rectClip.
     */
    private val pathClip = Path()

    /**
     * @pathBorder для рамки "видоискателя"
     */
    private val pathBorder = Path()

    /**
     * Paint (color Yellow)
     */
    private val paintStroke: Paint = Paint().apply {
        color = Color.argb(0xff, 0xff, 0xff, 0x0)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    /**
     * Темный полупрозрачный цвет
     */
    private val darkShadeColor = Color.argb(0x88, 0x00, 0x00, 0x00)

    /**
     * Ширина линии рамки
     */
    private val borderWidth = context.resources.getDimension(R.dimen.view_port_border_stroke_width)

    /**
     * Небольшой отступ линии рамки от внешних границ viewPort'а
     */
    private val borderMargin = context.resources.getDimension(R.dimen.view_port_border_margin)

    /**
     * Радиус закругления рамки видоискателя
     */
    private val cornerRadius =
        context.resources.getDimension(R.dimen.view_port_border_corner_radius)

    /**
     * Квадратные области для масштабирования рамки.
     */
    private val tapSquares: Map<TapArea, RectF> by rectTapDelegate()

    /**
     * @offsetV - это дополнительный вертикальный offset учитывающий жест перетаскивания
     * @offsetH - это дополнительный горизонтальный offset учитывающий жест перетаскивания
     */
    private var offsetV = 0f
    private var offsetH = 0f

    /**
     * Отслеживаем движения пальца в жестах
     */
    private var lastX = 0f
    private var lastY = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {

        return when (event.action) {

            MotionEvent.ACTION_DOWN -> {

                // Определить режим жеста и создать Gesture (для скалинга)
                detectMode(event.x, event.y)

                lastX = event.x
                lastY = event.y

                // Если собираемся перетаскивать, то нужно установить rectAnchor
                // на текущую позицию rectClip и сбросить offsetH/offsetV.
                // NOTE: Если собираемся скалировать, то offsetV/offsetH не изменяем,
                // так как по ним выставлен rectClip.
                if (mode == Mode.Dragging) {
                    rectAnchorMove()
                    offsetV = 0f
                    offsetH = 0f
                }

                // Вернуть true, если палец внутри рамки.
                mode != Mode.Waiting
            }
            MotionEvent.ACTION_MOVE -> {
                val dX = event.x - lastX
                val dY = event.y - lastY

                lastX = event.x
                lastY = event.y

                when (mode) {
                    is Mode.Dragging -> {
                        offsetV += dY
                        offsetH += dX
                        // Спозиционировать rectClip/pathClip. rectClip автоматически
                        // выравнивается, чтобы не выходить за границы.
                        preDragging()
                        // Теперь нужно проверить overhead. Если есть, то сдвинуть
                        // битмапу.
                        checkOverhead(dX, dY)?.let(viewModel::onOverHead)
                    }
                    is Mode.Scaling -> {

                        mode = gestureDetector.detectScalingSubMode(dX)
                        val offset = gestureDetector.onMove(dX, dY)

                        if (offset.invalid || offset.zero) {
                            return true
                        }

                        // TODO Нужно разобраться с checkBounds(). Возможно придется делать
                        // TODO две как в версии 3
                        offsetH = offset.x
                        offsetV = offset.y
                        preScalingBounds()
                    }
                    else -> {
                        logIt("unknown mode")
                    }
                }

                preDrawing() // Настроить pathBorder/paintStroke для рисования рамки
                invalidate()

                true
            }
            MotionEvent.ACTION_UP -> {
                /**
                 * NOTE: При первом запуске рамка целиком занимает одну из сторон rectVisible
                 * (в зависимости от ориентации экрана). Далее мы можем её уменьшить, а затем
                 * увеличить. Но до тех пор пока не уменьшили меньше rectMin хотя бы один раз -
                 * никакой зум не работает. После первого прохождения этой границы включается зум
                 * в обе стороны (и видимо снова должен отключаться, если полностью растянем рамку
                 * по одной из сторон rectVisible)
                 */

                when (mode) {
                    // Сжимаем
                    is Mode.Scaling.Squeeze -> {

                        // NOTE: Для анимации нужно чтобы 'rectClip.width <= rectMin.width'
                        scaleController.onScaleUpAvailable(rectClip.width() <= rectMin.width())

                        val bitmapScaleFactor = gestureDetector.scaleRatio

                        if (animationScaleUpAvailable) {
                            scaleController.onScaleRequired(bitmapScaleFactor, calculatePivot())
                        }
                    }
                    // Растягиваем
                    is Mode.Scaling.Shrink -> {
                        val bitmapScaleFactor = gestureDetector.scaleRatio

                        if (animationScaleDownAvailable) {
                            scaleController.onScaleRequired(bitmapScaleFactor, calculatePivot())
                        }
                    }
                    else -> {
                    }
                }
                // После поднятия пальца анонсим размеры rectClip
                viewModel.onRectClip(rectClip)
                true
            }
            else -> false
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        scaleController.onFrontSizeChanged(w to h)
    }

    /**
     * Размер View известен, битмапа загружена.
     * Инициализировать все вспомогательные структуры.
     */
    override fun onBitmapReady(mediaData: MediaData) {
        // Расчет
        super.onBitmapReady(mediaData)

        resetState()
        rectAnchorInit()
        preDragging()
        preDrawing()
        invalidate()

        // После первого позиционирования сразу анонсим rectClip, чтобы по кнопке Ready
        // иметь возможность получить битмапу начального состояния видоискателя.
        viewModel.onRectClip(rectClip)
    }

    /**
     * --------------------------------------------------------------------------------------
     * Позиционирование / Скалирование
     * --------------------------------------------------------------------------------------
     */

    /**
     * Для временных данных при работе анимации
     */
    private val rectTemp = RectF()
    private var pivot: PointF? = null

    private lateinit var gestureDetector: GestureDetector

    /**
     * Скалируемся от текущего размера до rectMin относительно pivot.
     */
    override fun onPreAnimate(scaleFactor: Float, pivot: PointF) {

        scaleFrom = rectClip.width()
        scaleTo = rectMin.width()
        this.pivot = rectClip.center
    }

    override fun onAnimate(fraction: Float) {

        requireNotNull(pivot)

        val rectDim = evaluator.evaluate(fraction, scaleFrom, scaleTo)

        // Установить rect по центру относительно pivot'a
        rectTemp.apply {
            left = pivot!!.x - rectDim / 2f
            top = pivot!!.y - rectDim / 2f
            right = left + rectDim
            bottom = top + rectDim
        }

        // Проверить крайние условия по X
        val offsetX = when {
            rectTemp.left < rectViewPort.left -> rectViewPort.left - rectTemp.left
            rectTemp.right > rectViewPort.right -> rectViewPort.right - rectTemp.right
            else -> 0f
        }

        // Проверить крайние условия по Y
        val offsetY = when {
            rectTemp.top < rectViewPort.top -> rectViewPort.top - rectTemp.top
            rectTemp.bottom > rectViewPort.bottom -> rectViewPort.bottom - rectTemp.bottom
            else -> 0f
        }

        rectTemp.offset(offsetX, offsetY)
        rectClip.set(rectTemp)
        preScalingBounds()
        preDrawing()
    }

    override fun onPostAnimate() {
        viewModel.onRectClip(rectClip)
    }

    /**
     * Вычислить pivot для скалирования. Если rectClip прижат какой-то стороной
     * к стороне rectVisible, то отталкиваемся от этой стороны. Иначе pivot'ом
     * становится центр rectClip.
     */
    private fun calculatePivot(): PointF {
        val xPivot = when {
            floor(rectClip.left).toInt() == rectViewPort.left -> rectClip.left
            floor(rectClip.right).toInt() == rectViewPort.right -> rectClip.right
            else -> rectClip.centerX()
        }

        val yPivot = when {
            floor(rectClip.top).toInt() == rectViewPort.top -> rectClip.top
            floor(rectClip.bottom).toInt() == rectViewPort.bottom -> rectClip.bottom
            else -> rectClip.centerY()
        }

        return PointF(xPivot, yPivot)
    }

    /**
     * Выбрать режим в зависимости от позиции касания.
     * Если ткнули в квадраты по краям viewport'а, то масштабируем, иначе передвигаем.
     */
    private fun detectMode(x: Float, y: Float) {

        val rectClipCopy = RectF(rectClip)

        tapSquares.entries.forEach { entry ->

            val (area, rect) = entry
            if (rect.contains(x, y)) {

                val (cornerX, cornerY) = when (area) {
                    is lt -> rectClipCopy.left to rectClipCopy.top
                    is lb -> rectClipCopy.left to rectClipCopy.bottom
                    is rt -> rectClipCopy.right to rectClipCopy.top
                    is rb -> rectClipCopy.right to rectClipCopy.bottom
                }

                /**
                 * Длина стороны rectClip разбивается на две части:
                 * - первая, с изменяемым размером, работает регулятором зума.
                 * - вторая, с фиксированным размером, показывает тот миниммальный размер видимой
                 *   части битмапы, которого нужно достич для максимального зума.
                 * Например, при первом показе битмапы на экране, отношение размеров фиксированной
                 * и изменяемой частей такое же как отношение
                 * rectBitmapVisibleHeightMin / (rectBitmapVisible.height - rectBitmapVisibleHeightMin).
                 *
                 * Если мы выполняем squeeze, то rectBitmapVisible уменьшается, соответственно
                 * в его height возрастает процентная доля высоты rectBitmapVisibleHeightMin.
                 * Когда они сравняются по величине, то зум станет максимальным.
                 */
                gestureDetector = GestureDetector(
                    TapCorner(area, PointF(x, y), PointF(cornerX, cornerY)),
                    Ratio(scaleController.bitmapScaleCurrent, scaleController.bitmapScaleMin),
                    Ratio(
                        1f,
                        min(1f, scaleController.bitmapScaleMin / scaleController.bitmapScaleCurrent)
                    ),
                    rectClipCopy.width()
                )

                mode = Mode.Scaling.Init
                return
            }
        }

        mode = if (rectClipCopy.contains(x, y)) {
            Mode.Dragging
        } else {
            Mode.Waiting
        }
    }

    /**
     * Здесь нужно отслеживать, чтобы при растяжении результирующий прямоугольник не выходил
     * за границы rectVisible.
     */
    private fun preScalingBounds() {
        // Скопировать из rectClip, проверить, что не выходим за границы экрана и сдвинуть
        rectClipShifted.apply {
            set(rectClip)
            if (mode == Mode.Scaling.Shrink) {
                checkBounds(this)
            }
            offset(offsetH, offsetV)
        }

        // Корректируем размеры rectClip
        when (mode) {
            // Палец идет от центра ViewPort'а (растягиваем)
            Mode.Scaling.Shrink -> {
                shrinkClipping()
            }
            // Палец идет к центру ViewPort'а (уменьшаем)
            Mode.Scaling.Squeeze -> {
                squeezeClipping()
            }
            else -> {
                return
            }
        }

        pathClip.apply {
            reset()
            addRoundRect(rectClip, cornerRadius, cornerRadius, Path.Direction.CW)
        }.close()

        // Также нужно обновить положение и размер rectAnchor. В режиме Scaling он перемещается
        // за rectClip, чтобы потом в режиме dragging сразу иметь правильную опорную точку.
        rectAnchorMove()
    }

    /**
     * Растягиваем квадрат. Вычислить объединение.
     */
    private fun shrinkClipping() {
        rectClip.union(rectClipShifted)
    }

    /**
     * Сжимаем ViewPort. Вычислить перечечение.
     */
    private fun squeezeClipping() {
        rectClip.intersect(rectClipShifted)
    }

    /**
     * Установить размер и положение rectAnchor по параметрам rectClip.
     * Главное, чтобы в момент выполнения set(rectClip) в переменных offsetH/offsetV были
     * актуальные значение, потому что rectClip вычисляется делегатом и использует эти поля.
     */
    private fun rectAnchorMove() {
        rectAnchor.set(rectClip)
    }

    /**
     * Опорный квадрат от которого будем смещать rectClip/rectBorder
     */
    private fun rectAnchorInit() {

        val isVertical = rectViewPort.height() >= rectViewPort.width()
        val frameDimen = min(rectViewPort.height(), rectViewPort.width())

        rectAnchor.apply {
            top = if (isVertical) {
                rectViewPort.top + (rectViewPort.height() - frameDimen) / 2f
            } else {
                rectViewPort.top.toFloat()
            }

            bottom = top + frameDimen

            left = if (isVertical) {
                rectViewPort.left.toFloat()
            } else {
                rectViewPort.left + (rectViewPort.width() - frameDimen) / 2f
            }
            right = left + frameDimen
        }
    }

    /**
     * --------------------------------------------------------------------------------------
     * Рисование
     * --------------------------------------------------------------------------------------
     */

    /**
     * К моменту вызова onDraw все вычисления уже выполнены.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        sourceImageBitmap?.let {
            try {
                // 1. Затененная область
                drawLayer1(canvas)
                // 2. Рамка вокруг выделенной области
                drawLayer2(canvas)
            } catch (e: Exception) {
            }
        }
    }

    /**
     * Выделенная область
     */
    private fun drawLayer1(canvas: Canvas) {
        canvas.save()
        setOuterClipping(canvas)
        canvas.drawColor(darkShadeColor)
        canvas.restore()
    }

    /**
     * Рамка вокруг выделенной области
     */
    private fun drawLayer2(canvas: Canvas) {

        canvas.save()
        setInnerClipping(canvas)

        // Нужно каждый раз переустаналивать strokeWidth иначе она становится hairline
        paintStroke.strokeWidth = borderWidth
        canvas.drawPath(pathBorder, paintStroke)

        canvas.restore()
    }

    private fun setOuterClipping(canvas: Canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas.clipOutPath(pathClip)
        } else {
            canvas.clipPath(pathClip, Region.Op.DIFFERENCE)
        }
    }

    private fun setInnerClipping(canvas: Canvas) {
        canvas.clipPath(pathClip)
    }

    /**
     * Подготовить новый pathClip для рисования
     */
    private fun preDragging() {
        pathClip.apply {
            reset()
            addRoundRect(rectClip, cornerRadius, cornerRadius, Path.Direction.CW)
        }.close()
    }

    /**
     * Сформировать Path для рисования рамки.
     */
    private fun preDrawing() {
        pathBorder.apply {
            reset()
            // Начальная позиция ниже rectBorder.left/tor на 1/4 его высоты.
            moveTo(rectBorder.left, rectBorder.top + rectBorder.height() / 4f)
            lineTo(rectBorder.left, rectBorder.top)
            lineTo(rectBorder.left + rectBorder.width(), rectBorder.top)
            lineTo(rectBorder.left + rectBorder.width(), rectBorder.top + rectBorder.height())
            lineTo(rectBorder.left, rectBorder.top + rectBorder.height())
            close()
        }

        paintStroke.pathEffect =
            DashPathEffect(floatArrayOf(rectBorder.width() / 2f, rectBorder.width() / 2), 0f)
    }

    /**
     * Функция проверяет overhead - когда пытаемся двигать рамку за пределы экрана. В этом случае
     * нужно перемещать основную битмапу, чтобы мы могли перемещаться по её поверхности.
     *
     * Аргументы dX / dY должны быть ИНКРЕМЕНТАЛЬНЫМИ смещениями. Здесь нельзя использовать
     * offsetH / offsetV потому что они кумулятивные.
     */
    private fun checkOverhead(dX: Float, dY: Float): OverHead? {
        val rect = RectF(rectClip)

        val overHeadX = when {
            (dX < 0) -> {
                if ((rect.left.toInt() == rectViewPort.left) ||
                    (rect.left.toInt() + dX < rectViewPort.left)
                ) dX else 0f
            }
            (dX > 0) -> {
                if ((rect.right.toInt() == rectViewPort.right) ||
                    (rect.right.toInt() + dX > rectViewPort.right)
                ) dX else 0f
            }
            else -> 0f
        }

        val overHeadY = when {
            (dY < 0) -> {
                if ((rect.top.toInt() == rectViewPort.top) ||
                    (rect.top.toInt() + dY < rectViewPort.top)
                ) dY else 0f

            }
            (dY > 0) -> {
                if ((rect.bottom.toInt() == rectViewPort.bottom) ||
                    (rect.bottom.toInt() + dY > rectViewPort.bottom)
                ) dY else 0f
            }
            else -> 0f
        }

        // Сообщить про overHead
        return if (overHeadX != 0f || overHeadY != 0f) {
            OverHead(overHeadX, overHeadY)
        } else null
    }

    /**
     * --------------------------------------------------------------------------------------
     * Делегаты
     * --------------------------------------------------------------------------------------
     */

    /**
     * Делегат зависит от rectAnchor.
     *
     * После каждого события MotionEvent.ACTION_MOVE нужно спозиционировать rectClip. Для этого
     * сначала возвращаем его в исходное состояние (не позицию rectAnchor), а потом с этой позиции
     * делаем offset на offsetV/offsetH предварительно проверяя крайние условия и внося
     * корректировки в offsetV/offsetH, чтобы не вылезать за края.
     */
    private fun rectClipDelegate(): ReadWriteProperty<Any?, RectF> =
        object : ReadWriteProperty<Any?, RectF> {

            var rect = RectF()
            var prevOffsetH = 0f
            var prevOffsetV = 0f

            override fun getValue(thisRef: Any?, property: KProperty<*>): RectF {

                return when (mode) {
                    is Mode.Dragging, is Mode.Waiting -> {
                        rect.apply {
                            // 1. Сначала позиционируем на rectAnchor
                            set(rectAnchor)

                            // 2. Проверить крайние условия.
                            checkBounds()/*?.let(viewModel::onOverHead)*/

                            // 3. Затем смещаем от anchor на вычисленные offsetH, offsetV
                            offset(offsetH, offsetV)
                        }
                    }
                    is Mode.Scaling, is Mode.Animating -> {
                        // Возвращаем без смещения
                        rect
                    }
                }
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: RectF) {
                rect.set(value)
            }

            private fun checkBounds()/*: OverHead?*/ {

                if (prevOffsetH == offsetH && prevOffsetV == offsetV) return/* null*/

                // Определить overHead'ы (толкаем рамку за пределы экрана)
//                val overHeadX = when {
//                    (offsetH < 0) -> {
//                        if ((rect.left.toInt() == rectViewPort.left) ||
//                            (rect.left.toInt() + offsetH < rectViewPort.left)
//                        ) offsetH else 0f
//                    }
//                    (offsetH > 0) -> {
//                        if ((rect.right.toInt() == rectViewPort.right) ||
//                            (rect.right.toInt() + offsetH > rectViewPort.right)
//                        ) offsetH else 0f
//                    }
//                    else -> 0f
//                }
//
//                val overHeadY = when {
//                    (offsetV < 0) -> {
//                        if ((rect.top.toInt() == rectViewPort.top) ||
//                            (rect.top.toInt() + offsetV < rectViewPort.top)
//                        ) offsetV else 0f
//
//                    }
//                    (offsetV > 0) -> {
//                        if ((rect.bottom.toInt() == rectViewPort.bottom) ||
//                            (rect.bottom.toInt() + offsetV > rectViewPort.bottom)
//                        ) offsetV else 0f
//                    }
//                    else -> 0f
//                }

                // Определить overHead'ы
//                if ((rect.left.toInt() == rectViewPort.left && offsetH < 0) ||
//                    (rect.left.toInt() + offsetH < rectViewPort.left) ||
//                    (rect.right.toInt() == rectViewPort.right && offsetH > 0) ||
//                    (rect.right.toInt() + offsetH > rectViewPort.right)
//                ) {
//                    overHeadX = offsetH
//                }
//                if ((rect.top.toInt() == rectViewPort.top && offsetV < 0) ||
//                    (rect.bottom.toInt() == rectViewPort.bottom && offsetV > 0) ||
//                    (rect.top.toInt() + offsetV < rectViewPort.top) ||
//                    (rect.bottom.toInt() + offsetV > rectViewPort.bottom)
//                ) {
//                    overHeadY = offsetV
//                }

                // Контроль границ рамки
                if (rect.left + offsetH < rectViewPort.left) {
                    offsetH = rectViewPort.left - rect.left
                } else if (rect.right + offsetH > rectViewPort.right) {
                    offsetH = rectViewPort.right - rect.right
                }
                if (rect.top + offsetV < rectViewPort.top) {
                    offsetV = rectViewPort.top - rect.top
                } else if (rect.bottom + offsetV > rectViewPort.bottom) {
                    offsetV = rectViewPort.bottom - rect.bottom
                }

                prevOffsetH = offsetH
                prevOffsetV = offsetV

                // Сообщить про overHead
//                return if (overHeadX != 0f || overHeadY != 0f) {
//                    logIt("OverHead: x/y $overHeadX / $overHeadY, rect.left=${rect.left}, rectViewPort.left=${rectViewPort.left}")
//                    OverHead(overHeadX, overHeadY)
//                } else null
            }
        }

    /**
     * Делегат зависит от rectClip. Области тача для масштабирования рамки.
     */
    private fun rectTapDelegate(): ReadWriteProperty<Any?, Map<TapArea, RectF>> =
        object : ReadWriteProperty<Any?, Map<TapArea, RectF>> {

            val ratio = 0.25f
            val square = RectF()
            val squares = mapOf(
                lt to RectF(),
                rt to RectF(),
                rb to RectF(),
                lb to RectF()
            )
            val rectTmp = RectF()

            override fun getValue(thisRef: Any?, property: KProperty<*>): Map<TapArea, RectF> {

                rectTmp.set(rectClip)

                square.apply {
                    set(rectTmp)
                    scale(ratio, ratio)
                }

                squares.entries.forEach { e ->
                    when (e.key) {
                        lt -> {
                            e.value.set(square)
                        }
                        rb -> {
                            e.value.apply {
                                set(square)
                                offset(
                                    rectTmp.width() - square.width(),
                                    rectTmp.height() - square.height()
                                )
                            }
                        }
                        rt -> {
                            e.value.apply {
                                set(square)
                                offset(rectTmp.width() - square.width(), 0f)
                            }
                        }
                        lb -> {
                            e.value.apply {
                                set(square)
                                offset(0f, rectTmp.height() - square.height())
                            }
                        }
                    }
                }

                return squares
            }

            override fun setValue(
                thisRef: Any?,
                property: KProperty<*>,
                value: Map<TapArea, RectF>
            ) {
            }
        }

    /**
     * Делегат зависит от rectClip
     */
    private fun rectBorderDelegate(): ReadWriteProperty<Any?, RectF> =
        object : ReadWriteProperty<Any?, RectF> {

            var rect = RectF()
            var rectTmp = RectF()

            override fun getValue(thisRef: Any?, property: KProperty<*>): RectF {

                rectTmp.set(rectClip)

                rect.apply {
                    left = rectTmp.left + borderWidth / 2f + borderMargin
                    top = rectTmp.top + borderWidth / 2f + borderMargin
                    right = rectTmp.right - borderWidth / 2f - borderMargin
                    bottom = rectTmp.bottom - borderWidth / 2f - borderMargin
                }
                return rect
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: RectF) {}
        }

    /**
     * Минимальный размер для viewPort. Меньше этого размера viewPort не может
     * быть на эране когда нет касаний.
     */
    private fun rectMinDelegate(): ReadWriteProperty<Any?, RectF> =
        object : ReadWriteProperty<Any?, RectF> {

            var rect = RectF()

            override fun getValue(thisRef: Any?, property: KProperty<*>): RectF {
                val dimension = min(rectViewPort.width(), rectViewPort.height()) / 5f * 3f
                rect.set(0f, 0f, dimension, dimension)
                return rect
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: RectF) {
                rect.set(value)
            }
        }

    /**
     * --------------------------------------------------------------------------------------
     * Утилитные функции
     * --------------------------------------------------------------------------------------
     */

    /**
     * Проверить, что прямоугольник @rect не выходит за границы rectVisible.
     * И если выходит, то поправить offsetH/offsetV.
     */
    private fun checkBounds(rect: RectF) {

        if (rect.left + offsetH < rectViewPort.left) {
            offsetH = rectViewPort.left - rect.left
        } else if (rect.right + offsetH > rectViewPort.right) {
            offsetH = rectViewPort.right - rect.right
        }

        if (rect.top + offsetV < rectViewPort.top) {
            offsetV = rectViewPort.top - rect.top
        } else if (rect.bottom + offsetV > rectViewPort.bottom) {
            offsetV = rectViewPort.bottom - rect.bottom
        }

        val offset = min(abs(offsetH), abs(offsetV))
        offsetV = offset * sign(offsetV)
        offsetH = offset * sign(offsetH)
    }

    /**
     * Сбросить состояние в исходное
     */
    private fun resetState() {
        offsetH = 0f
        offsetV = 0f
        lastX = 0f
        lastX = 0f
        mode = Mode.Waiting
    }
}
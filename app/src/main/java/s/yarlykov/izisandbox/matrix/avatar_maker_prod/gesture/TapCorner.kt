package s.yarlykov.izisandbox.matrix.avatar_maker_prod.gesture

import android.graphics.PointF

//
//              rectClip
//           _______________
//          |               |
//          |               |
//          |    TapArea (LeftBottom квадрат внутри rectClip)
//          |____
//          |  * | *-tap
//          0____|__________|
//       0-pivot
//

/**
 * @param tapArea - область тача
 * @param tap - X/Y-координата тача
 * @param pivot - X/Y-координата угла
 */
data class TapCorner(val tapArea: TapArea, val tap : PointF, val pivot: PointF)
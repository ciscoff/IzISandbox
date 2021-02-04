package s.yarlykov.izisandbox.matrix.avatar_maker.gesture

//
//              rectClip
//           _______________
//          |               |
//          |               |
//          |    TapArea (LeftBottom квадрат внутри rectClip)
//          |____
//          |  * | *-tapX
//          0____|__________|
//       0-cornerX
//

/**
 * @param tapArea - область тача
 * @param tapX - X-координата тача
 * @param cornerX - X-координата угла области
 */
data class TapCorner(val tapArea: TapArea, val tapX : Float, val cornerX: Float)
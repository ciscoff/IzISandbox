package s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.factories

import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain.DoerDatum
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain.DoerRole
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain.role

/**
 * UnderLayer состоит из двух половин. Каждая содержит фон, а также надпись или иконку.
 * Данный класс определяет цвета фона, строки для надписей и иконки.
 */
object UnderLayerStateFactory {

    fun createForDoer(user: DoerDatum, isConfirmed: Boolean): UnderLayerState {
        return when (user.role()) {
            DoerRole.Manager, DoerRole.Admin -> {
                if (isConfirmed) {
                    UnderLayerState(
                        R.drawable.background_slider_round_yellow,
                        R.string.doer_action_status,
                        null,
                        R.drawable.background_slider_round_red,
                        null,
                        R.drawable.vd_delete_white
                    )
                } else {
                    UnderLayerState(
                        R.drawable.background_slider_round_blue,
                        R.string.doer_action_confirm,
                        null,
                        R.drawable.background_slider_round_red,
                        null,
                        R.drawable.vd_delete_white
                    )
                }
            }
            else -> {
                if (isConfirmed) {
                    UnderLayerState(
                        R.drawable.background_slider_round_red,
                        R.string.doer_action_refuse,
                        null,
                        R.drawable.background_slider_round_red,
                        R.string.doer_action_refuse,
                        null
                    )
                } else {
                    UnderLayerState(
                        R.drawable.background_slider_round_blue,
                        R.string.doer_action_confirm,
                        null,
                        R.drawable.background_slider_round_blue,
                        R.string.doer_action_confirm,
                        null
                    )
                }
            }
        }
    }

    fun createForResource(user: DoerDatum): UnderLayerState {
        return when (user.role()) {
            DoerRole.Manager, DoerRole.Admin -> {
                UnderLayerState(
                    R.drawable.background_slider_round_red,
                    null,
                    R.drawable.vd_delete_white,
                    R.drawable.background_slider_round_red,
                    null,
                    R.drawable.vd_delete_white)
            }
            else -> {
                UnderLayerState(R.drawable.background_slider_round_red,
                    null,
                    null,
                    R.drawable.background_slider_round_red,
                    null,
                    null)
            }
        }
    }

}
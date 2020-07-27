package s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.factories

import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain.DoerDatum
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain.DoerRole
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain.role

/**
 * UnderLayer состоит из двух половин. Каждая содержит фон, а также надпись или иконку.
 * Данный класс определяет цвета фона, строки для надписей и иконки.
 */
object UnderLayerStateFactoryV2 {

    fun createForDoer(user: DoerDatum, isConfirmed: Boolean): UnderLayerStateV2 {
        return when (user.role()) {
            DoerRole.Manager, DoerRole.Admin -> {
                if (isConfirmed) {
                    UnderLayerStateV2(
                        // Цвет фона для начального состояния
                        R.color.doer_state_confirmed_light,
                        null,
                        // AVD для "прямой" анимации
                        R.drawable.avd_from_confirmed_to_waiting,
                        // Цвет фона в который анимируемся
                        R.color.doer_state_waiting_light,
                        null,
                        // AVD для обратной анимации
                        R.drawable.avd_from_waiting_to_confirmed
                    )
                } else {
                    UnderLayerStateV2(
                        R.color.doer_state_waiting_light,
                        null,
                        R.drawable.avd_from_confirmed_to_waiting,
                        R.color.doer_state_confirmed,
                        null,
                        R.drawable.vd_doer_state_white
                    )
                }
            }
            else -> {
                if (isConfirmed) {
                    UnderLayerStateV2(
                        R.color.doer_state_refused_light,
                        R.string.doer_action_refuse,
                        null,
                        R.color.doer_state_refused,
                        R.string.doer_action_refuse,
                        null
                    )
                } else {
                    UnderLayerStateV2(
                        R.color.doer_state_refused_light,
                        R.string.doer_action_confirm,
                        null,
                        R.color.doer_state_refused,
                        R.string.doer_action_confirm,
                        null
                    )
                }
            }
        }
    }

    fun createForResource(user: DoerDatum): UnderLayerStateV2 {
        return when (user.role()) {
            DoerRole.Manager, DoerRole.Admin -> {
                UnderLayerStateV2(
                    R.drawable.background_slider_round_red,
                    null,
                    R.drawable.vd_delete_white,
                    R.drawable.background_slider_round_red,
                    null,
                    R.drawable.vd_delete_white)
            }
            else -> {
                UnderLayerStateV2(R.drawable.background_slider_round_red,
                    null,
                    null,
                    R.drawable.background_slider_round_red,
                    null,
                    null)
            }
        }
    }

}
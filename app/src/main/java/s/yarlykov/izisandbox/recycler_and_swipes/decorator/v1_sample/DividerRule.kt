package s.yarlykov.izisandbox.recycler_and_swipes.decorator.v1_sample

import androidx.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@IntDef(
    Rules.MIDDLE,
    Rules.END
)
annotation class DividerRule
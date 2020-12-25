package s.yarlykov.izisandbox.recycler_and_swipes.decorator

import androidx.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@IntDef(
    Rules.MIDDLE,
    Rules.END
)
annotation class DividerRule
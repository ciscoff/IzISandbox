package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_02

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import s.yarlykov.izisandbox.Utils.logIt
import s.yarlykov.izisandbox.extensions.ZDate
import s.yarlykov.izisandbox.extensions.toReadable
import java.util.*

class InfiniteModel(model: MutableList<ZDate>) : MutableList<ZDate> by model, OverScrollListener {

    override fun onTopOverScroll(millis: Long) {
        val instant = Instant.ofEpochMilli(millis)
        val topDate = ZDate.ofInstant(instant, ZoneId.systemDefault())

        Collections.rotate(this, 1)
        this[0] = topDate.minusDays(1)
        logIt("onTopOverScroll new model:", "PLPLPL")
        this.forEach {
            logIt("... $it", "PLPLPL")
        }
    }

    override fun onBottomOverScroll(millis: Long) {
        val instant = Instant.ofEpochMilli(millis)
        val bottomDate = ZDate.ofInstant(instant, ZoneId.systemDefault())

        Collections.rotate(this, -1)
        this[0] = bottomDate.plusDays(1)

        logIt("onTopOverScroll new model:", "PLPLPL")
        this.forEach {
            logIt("... $it", "PLPLPL")
        }
    }
}
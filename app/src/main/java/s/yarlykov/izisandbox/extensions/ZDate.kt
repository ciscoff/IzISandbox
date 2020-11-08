package s.yarlykov.izisandbox.extensions

import android.content.Context
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import s.yarlykov.izisandbox.R
import java.util.*

private const val readableFormatEee = "EEE, dd MMMM"
private const val readableFormatName = ", dd MMMM"

private const val readableFormatEeePlusTime = "EEE, dd MMMM HH:mm"
private const val readableFormatNamePlusTime = ", dd MMMM HH:mm"

/**
 * Дата в формате "ЧТ, 20 АВГУСТА", "СЕГОДНЯ, 08 ОКТЯБРЯ", "ВЧЕРА, 12 ИЮНЯ"
 */
fun ZDate.toReadable(context: Context): String {

    val readableFormatterEee: DateTimeFormatter = DateTimeFormatter.ofPattern(readableFormatEee)
    val readableFormatterName: DateTimeFormatter = DateTimeFormatter.ofPattern(readableFormatName)

    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
    val yesterday = today.minusDays(1)
    val date = toLocalDate()

    val prefixId = when {
        date.isEqual(today) -> R.string.ui_day_today
        date.isEqual(yesterday) -> R.string.ui_day_yesterday
        date.isEqual(tomorrow) -> R.string.ui_day_tomorrow
        else -> 0
    }

    return if (prefixId != 0) {
        "${context.getString(prefixId)}${format(readableFormatterName)}"
    } else {
        format(readableFormatterEee)
    }.toUpperCase(Locale.getDefault())
}

/**
 * Дата в формате "ЧТ, 20 АВГУСТА", "СЕГОДНЯ, 08 ОКТЯБРЯ", "ВЧЕРА, 12 ИЮНЯ"
 */
fun LocalDateTime.toReadable(context: Context): String {

    val readableFormatterEee: DateTimeFormatter = DateTimeFormatter.ofPattern(readableFormatEeePlusTime)
    val readableFormatterName: DateTimeFormatter = DateTimeFormatter.ofPattern(readableFormatNamePlusTime)

    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
    val yesterday = today.minusDays(1)
    val date = toLocalDate()

    val prefixId = when {
        date.isEqual(today) -> R.string.ui_day_today
        date.isEqual(yesterday) -> R.string.ui_day_yesterday
        date.isEqual(tomorrow) -> R.string.ui_day_tomorrow
        else -> 0
    }

    return if (prefixId != 0) {
        "${context.getString(prefixId)}${format(readableFormatterName)}"
    } else {
        format(readableFormatterEee)
    }.toUpperCase(Locale.getDefault())
}

fun LocalDate.toReadable(context: Context, withDetails: Boolean = false): String {

    val readableFormatterEee: DateTimeFormatter = DateTimeFormatter.ofPattern(readableFormatEee)
    val readableFormatterName: DateTimeFormatter = DateTimeFormatter.ofPattern(readableFormatName)

    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
    val yesterday = today.minusDays(1)

    val date = this

    val prefixId = if (withDetails) {
        when {
            date.isEqual(today) -> R.string.ui_day_today
            date.isEqual(yesterday) -> R.string.ui_day_yesterday
            date.isEqual(tomorrow) -> R.string.ui_day_tomorrow
            else -> 0
        }
    } else 0

    return if (prefixId != 0) {
        "${context.getString(prefixId)}${format(readableFormatterName)}"
    } else {
        format(readableFormatterEee)
    }.toUpperCase(Locale.getDefault())
}
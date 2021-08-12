package s.yarlykov.izisandbox.datetime

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

const val dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ssZ"
const val dateFormat = "dd.MM.yyyy"

val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat)
val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)

fun ZonedDateTime.toSz(dateOnly: Boolean = false): String {
    return format(if (dateOnly) dateFormatter else dateTimeFormatter)
}

fun Long.toZdt() : ZonedDateTime {
    return ZonedDateTime.ofInstant(Date(this).toInstant(), ZoneId.systemDefault())
}

fun String.toZdt() : ZonedDateTime {
    return this.toLong().toZdt()
}

fun main() {
    val zdtNow = ZonedDateTime.ofInstant(Date().toInstant(), ZoneId.systemDefault())
    println(zdtNow.toSz())
    println(System.currentTimeMillis().toZdt().toSz())
    println(System.currentTimeMillis().toString().toZdt().toSz())
    println(System.currentTimeMillis().toString())
}
package sk.csirt.viruschecker.driver.scheduled

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer

data class TimePeriod(val value: Long, val unit: TimeUnit) {
    fun toMilliseconds(): Long = unit.toMillis(value)
}


data class TimeScheduler(
    val start: LocalTime,
    val period: TimePeriod
) {
    private val tasks = mutableListOf<Timer>()

    fun addTask(action: TimerTask.() -> Unit) {
        tasks.add(
            timer(
                action = action,
                startAt = LocalDateTime.of(LocalDate.now(), start)
                    .atZone(ZoneId.systemDefault()).toInstant()
                    .let { Date.from(it) },
                period = period.toMilliseconds()
            )
        )
    }
}

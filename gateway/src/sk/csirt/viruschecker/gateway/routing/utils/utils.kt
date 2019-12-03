package sk.csirt.viruschecker.gateway.routing.utils

import org.jetbrains.exposed.sql.LiteralOp
import org.jetbrains.exposed.sql.VarCharColumnType
import org.joda.time.DateTime
import java.time.Instant

fun DateTime.toJavaTimeInstant(): Instant = this.millis.let { Instant.ofEpochMilli(it) }

fun Instant.toJodaDateTime(): DateTime = this.toEpochMilli().let { DateTime(it) }

fun String.toOp() = LiteralOp(VarCharColumnType(), this)
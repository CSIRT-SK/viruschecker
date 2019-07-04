package driver.config


import driver.antivirus.*
import driver.scheduled.TimePeriod
import driver.scheduled.TimeScheduler
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object Properties {

    //    val keepReportsDays by lazy { "keep.reports.days".let { it with storage[it] }}
    val keepReportsDays = "keep.reports.days"
    val updateTime = "update.start"
    val updateIntervalDays = "update.start.interval"
    val scanTimeout = "scan.timeout.millis"

    object Avast {
        val command = "avast"
        val reportFlag = "avast.flag.report"

        object Scan {
            val flag = "avast.scan.flag"
            val additionalOptions = "avast.scan.flag.additional"
        }
    }

    object Eset {
        val command = "eset"
        val reportFlag = "eset.flag.report"

        object Scan {
            val flag = "eset.scan.flag"
            val additionalOptions = "eset.scan.flag.additional"
        }
    }

    object Kaspersky {
        val command = "kaspersky"
        val reportFlag = "kaspersky.flag.report"

        object Scan {
            val flag = "kaspersky.scan.flag"
            val additionalOptions = "kaspersky.scan.flag.additional"
        }

        object Update {
            val flag = "kaspersky.update.flag"
            val additionalOptions = "kaspersky.update.flag.additional"
        }
    }
}

//val updateScheduler = named("nightUpdater")
//val cleanScheduler = named("cleaner")

val driverDependencyInjectionModule = module {

    single<Antivirus>(AntivirusType.AVAST) {
        Avast(
            scanCommand = ExecutableCommand(
                executableName = getProperty(Properties.Avast.command),
                flag = getProperty(Properties.Avast.Scan.flag),
                additionalOptions = getProperty(Properties.Avast.Scan.additionalOptions),
                reportFlag = getProperty(Properties.Avast.reportFlag),
                timeout = getProperty(Properties.scanTimeout)
            )
        )
    }

    single<Antivirus>(AntivirusType.ESET) {
        Eset(
            scanCommand = ExecutableCommand(
                executableName = getProperty(Properties.Eset.command),
                flag = getProperty(Properties.Eset.Scan.flag),
                additionalOptions = getProperty(Properties.Eset.Scan.additionalOptions),
                reportFlag = getProperty(Properties.Eset.reportFlag),
                timeout = getProperty(Properties.scanTimeout)
            )
        )
    }

    single<Antivirus>(AntivirusType.KASPERSKY) {
        Kaspersky(
            scanCommand = ExecutableCommand(
                executableName = getProperty(Properties.Kaspersky.command),
                flag = getProperty(Properties.Kaspersky.Scan.flag),
                additionalOptions = getProperty(Properties.Kaspersky.Scan.additionalOptions),
                reportFlag = getProperty(Properties.Kaspersky.reportFlag),
                timeout = getProperty(Properties.scanTimeout)
            ),
            updateCommand = ExecutableCommand(
                executableName = getProperty(Properties.Kaspersky.command),
                flag = getProperty(Properties.Kaspersky.Update.flag),
                additionalOptions = getProperty(Properties.Kaspersky.Update.additionalOptions),
                reportFlag = getProperty(Properties.Kaspersky.reportFlag),
                timeout = getProperty(Properties.scanTimeout)
            )
        )
    }

//    single(updateScheduler) {
//        TimeScheduler(
//            start = getProperty<String>(Properties.updateTime).let {
//                LocalTime.parse(it)
//            },
//            period = getProperty<Long>(Properties.updateIntervalDays).let {
//                    TimePeriod(it, TimeUnit.DAYS)
//            }
//        )
//    }
//
//    single(cleanScheduler) {
//        TimeScheduler(
//            start = LocalTime.now(),
//            period = getProperty<Long>(Properties.keepReportsDays).let {
//                TimePeriod(it, TimeUnit.DAYS)
//            }
//        )
//    }

//    single<Int>(named(Properties.keepReportsDays)) { getProperty(Properties.keepReportsDays) }


}
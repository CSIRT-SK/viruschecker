package driver.config

import utils.createDirectoryIfNotExists

object Constants {
    val scanDir = "scandir".also { createDirectoryIfNotExists(it) }
    val scanReportsDir = "scanReports".also { createDirectoryIfNotExists(it) }
    val updateReportsDir = "updateReports".also { createDirectoryIfNotExists(it) }
}

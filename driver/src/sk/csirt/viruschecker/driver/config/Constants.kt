package sk.csirt.viruschecker.driver.config

import sk.csirt.viruschecker.driver.utils.createDirectoryIfNotExists

object Constants {
    val scanDir = "scandir".also { createDirectoryIfNotExists(it) }
    val scanReportsDir = "scanReports".also { createDirectoryIfNotExists(it) }
}

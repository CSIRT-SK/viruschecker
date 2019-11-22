package sk.csirt.viruschecker.driver.utils

import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Paths

private val logger = KotlinLogging.logger {}

fun parseParameter(
    commandList: MutableList<String>,
    parameter: String,
    value: String
) {
    when {
        parameter.endsWith(":") -> commandList.add(parameter + value)
        parameter.endsWith("=") -> commandList.add(parameter + value)
        parameter.isBlank() -> commandList.add(value)
        parameter == "(none)" -> commandList.add(value)
        else -> commandList.add(parameter).also { commandList.add(value) }
    }
}

fun createDirectoryIfNotExists(directoryName: String){
    val directoryPath = Paths.get(directoryName)
    if(Files.exists(directoryPath).not()){
        Files.createDirectory(directoryPath)
    }
}
package sk.csirt.viruschecker.driver.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

typealias ProcessInput = List<String>
typealias ProcessOutput = List<String>

class ProcessRunner {
    suspend fun runProcess(command: ProcessInput): ProcessOutput =
        withContext(Dispatchers.IO) {
            ProcessBuilder(command)
                .start()
                .inputStream
                .bufferedReader()
                .useLines {
                    it.toList()
                }
        }
}


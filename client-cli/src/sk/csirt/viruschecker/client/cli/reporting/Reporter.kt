package sk.csirt.viruschecker.client.cli.reporting

import java.io.File

interface Reporter<T>{
    fun saveReport(file: File, results: List<T>)
}
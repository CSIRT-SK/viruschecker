Modify the driver program
=========================

There are two types of modification described in this guide.
 
* Modification of commands used to launch antivirus scanner. 
* Adding support for another antivirus.


1 Modification of commands to launch antivirus scanner
------------------------------------------------------

Running the driver for the first time will generate a file named `viruschecker-driver.properties`.
Content of the file depends on the operating system.

For example, on Windows the content will look like this

```properties
# Avast
# ==============================================================================

avast=ashCmd.exe [SCAN-FILE] /_> [REPORT-FILE]

# Eset
# ==============================================================================

eset=ecls.exe [SCAN-FILE] /log-file=[REPORT-FILE] /log-all

# Kaspersky
# ==============================================================================

kaspersky=avp.com scan [SCAN-FILE] /RA:[REPORT-FILE] /i0

# Microsoft
# ==============================================================================

microsoft=MpCmdRun.exe -Scan -ScanType 3 -File [SCAN-FILE] -DisableRemediation

# VirusTotal
# ==============================================================================

virustotal.apikey=<insert-your-api-key>
```
while on Linux it will look like this
```properties
# Comodo
# ==============================================================================

comodo=/opt/COMODO/cmdscan -s [SCAN-FILE] -v

# VirusTotal
# ==============================================================================

virustotal.apikey=<insert-your-api-key>
```

Most properties in the above examples represents commands that are called when the driver receives 
some file to scan. Notice the two wildcards `[SCAN-FILE]` and `[REPORT-FILE]` representing the file 
to scan and the file to store scan report, respectively. 

Every property that represents the command to run the AV must contain the 
`[SCAN-FILE]` wildcard. 
In the case of commands without the `[REPORT-FILE]` wildcard it is
assumed that the scan report will be printed to the standard output.

### 1.1 I want some antivirus supported only on OS A to be supported on OS B 

For example, only Windows version of the Avast, Eset, Kaspersky and Microsoft Antivirus are right 
now supported. The opposite is true for Comodo where we only support the Linux version.

If you wish to use, for example, the Linux version of Kaspersky, then just provide the correct form 
of `kaspersky` property into the `viruschecker-driver.properties` that is automatically generated on 
Linux and run the driver as usual, i.e. `java -jar <name-of-driver> KASPERSKY`.
This should work as long as the both Windows and Linux version of the antivirus use the same report 
formats.  

### 1.2 VirusTotal API KEY

The VirusTotal API requires an API key to be set. 
You can obtain the free api key [here](https://support.virustotal.com/hc/en-us/articles/115002088769-Please-give-me-an-API-key).
Be noted that the free api key is limited to 4 requests per minute.
Otherwise you may need to purchase the premium api key.

Firstly you need to place your api key into `viruschecker-driver.properties`.
If you do not have this file, then just run the driver program without any arguments like 
```bash
java -jar <name-of-driver>
```
Open the file with any text editor that is better than *Notepad*.
In this line 
```bash
virustotal.apikey=<insert-your-api-key>
``` 
replace the `<insert-your-api-key>` with your api key.

2 Adding support for another antivirus
--------------------------------------

Some programming skill is required to follow this section.

This software is written in [*Kotlin*](https://kotlinlang.org/) programming language.
It is recommended, although not required, to use [*IntelliJ IDEA*](https://www.jetbrains.com/idea/)
to modify and compile the source codes. 
The open source and free *Community Edition* is sufficient.
  
This guide also assumes that the AV, for which you want to implement support, does support 
the command line scanning of a single file.
As of this moment, we deliberately support only such antivirus solutions.

In general, adding the support for a new AV comprises the following steps:

* Install the new AV. It is recommended to always do this on a VM.

* Disable all automatic features and protections(scanners, firewalls,...) of the AV except automatic
updates of the virus definitions database. 

* Find out the name and location of the command line scan utility provided by the antivirus.

* Scan some file and save the report somewhere. It will serve you as template for implementing the 
report parser. 

* Implement the scan report parser by extending the `CommandLineAntivirus` abstract class.

* Register new antivirus for `viruschecker-driver.properties`.

Now we will thoroughly explain these steps on the example of reimplementing the support for 
Kaspersky Antivirus.

Many antivirus programs contains command line utilities to scan some specific file.
These utilities will usually report the scan result in a simple text format.
Adding support for another antivirus is, for the most part, parsing the scan report.

Assume we want to reimplement the support for Kaspersky Antivirus on Windows.
Installation and configuration of this antivirus is already explained [here](/docs/driver/drivers-on-windows.md).

### 2.1 Learn about the antivirus

Before we can start to implement something, we need to do some recon.
Our task is now to learn the structure of the scan reports produced by AV's command line 
scanner.
 
It is recommended to scan at least four different files and save the reports.
The four files should be
* normal text or executable file,
* harmless virus test file - [`eicar.com`](https://www.eicar.org/?page_id=3950) 
* normal archive file like *zip*, *rar*, *jar*, *pdf*, ...
* archive file like the ones mentioned above, but containing the eicar.com 
(or download *eicar_com.zip*).

Some antivirus programs, including Kaspersky, treat archive files as directories.
Therefore the scan reports for archives needs to be treated specially. 
 
This antivirus provides the command line scan utility called *avp.com* located at `C:\Program Files (x86)\Kaspersky Lab\Kaspersky Free 19.0.0`.
To scan some file from the command line we can use this command
```bash
C:\Program Files (x86)\Kaspersky Lab\Kaspersky Free 19.0.0\avp.com scan <some-file> /RA:<report-file> /i0
```


###### Note
The `/i0` switch disables the automatic popup window asking for a user decision on how to treat the 
infected file which may halt the driver because it cannot deal with a graphical components of the 
Kaspersky antivirus. 
Instead, the driver program itself deletes all scanned files.  
 
#### 2.1.1 Scanning a healthy file

Create a file called `healthy.txt` in `C:\virus-checker` and write something into it.
Let us say we want to scan this file with Kaspersky command line scanner and save the report to 
`healthy-report.txt` in the same directory. 
This can be achieved with command
```bash
C:\Program Files (x86)\Kaspersky Lab\Kaspersky Free 19.0.0\avp.com scan C:\virus-checker\healthy.txt /RA:C:\virus-checker\healthy-report.txt
```

If you have placed the Kaspersky installation directory 
`C:\Program Files (x86)\Kaspersky Lab\Kaspersky Free 19.0.0` into *Path* system variable, you may
invoke the scanner using the shorter syntax.

```bash
avp.com scan C:\virus-checker\healthy.txt /RA:C:\virus-checker\healthy-report.txt /i0
```

###### Note: We will further assume that the Kaspersky installation directory is on the *Path*

Now open the `C:\virus-checker\healthy-report.txt`.
It's content should look similar to the text below.

```text
; --- Settings ---
; Action on detect:	Disinfect automatically
; Scan objects:	All objects
; Use iChecker:	Yes
; Use iSwift:	Yes
; Try disinfect:	No
; Try delete:	No
; Try delete container:	No
; Exclude by mask:	No
; Include by mask:	No
; Objects to scan:	
; 	"C:\virus-checker\healthy-report.txt"	Enable = Yes	Recursive = No
; ------------------
2019-06-28 09:37:03	Scan_Objects$0538         starting   1%         
2019-06-28 09:37:03 C:\virus-checker\healthy-report.txt	ok
2019-06-28 09:37:03	Scan_Objects$0538         running    100%       
2019-06-28 09:37:03	Scan_Objects$0538         completed             
;  --- Statistics ---
; Time Start:	2019-06-28 09:37:03
; Time Finish:	2019-06-28 09:37:03
; Processed objects:	1
; Total OK:	1
; Total detected:	0
; Suspicions:	0
; Total skipped:	0
; Password protected:	0
; Corrupted:	0
; Errors:	0
;  ------------------
```

Line `2019-06-28 09:37:03 C:\virus-checker\healthy-report.txt	ok` tells us that this file does 
not contain any known malware.

#### 2.1.2 Scanning an "infected" file

Place the [`eicar.com`](https://www.eicar.org/?page_id=3950) testing file in `C:\virus-checker` and
run the scanner.
 
```bash
avp.com scan C:\virus-checker\eicar.com /RA:C:\virus-checker\eicar-report.txt /i0
```

Now open the `C:\virus-checker\eicar-report.txt`.
It's content should look similar to the text below.

```text
; --- Settings ---
; Action on detect:	Disinfect automatically
; Scan objects:	All objects
; Use iChecker:	Yes
; Use iSwift:	Yes
; Try disinfect:	No
; Try delete:	No
; Try delete container:	No
; Exclude by mask:	No
; Include by mask:	No
; Objects to scan:	
; 	"C:\virus-checker\eicar.com "	Enable = Yes	Recursive = No
; ------------------
2019-06-28 09:50:17	Scan_Objects$0546         starting   1%         
2019-06-28 09:50:18	Scan_Objects$0546         running    1%         
2019-06-28 09:50:18 C:\virus-checker\eicar.com 	detected	EICAR-Test-File
2019-06-28 09:50:18	C:\virus-checker\eicar.com 	skipped
2019-06-28 09:50:18	Scan_Objects$0546         completed             
;  --- Statistics ---
; Time Start:	2019-06-28 09:50:17
; Time Finish:	2019-06-28 09:50:18
; Processed objects:	1
; Total OK:	0
; Total detected:	1
; Suspicions:	0
; Total skipped:	0
; Password protected:	0
; Corrupted:	0
; Errors:	0
;  ------------------
```

Line `2019-06-28 09:50:18 C:\virus-checker\eicar.com 	detected	EICAR-Test-File` tells us that 
this file does contain a "threat" recognized as `EICAR-Test-File`.
 
```bash
avp.com scan C:\virus-checker\eicar.com /RA:C:\virus-checker\eicar-report.txt /i0
```
#### 2.1.3 Scanning a healthy archive

In `C:\virus-checker`, create a file called `healthy.zip` by archiving at least two files, let's say `healthy1.txt` and `healthy2.txt`, and run the 
scanner.

```bash
avp.com scan C:\virus-checker\healthy.zip /RA:C:\virus-checker\healthy-zip-report.txt
```
Now open the `C:\virus-checker\healthy-zip-report.txt`.

```text
; --- Settings ---
; Action on detect:	Disinfect automatically
; Scan objects:	All objects
; Use iChecker:	Yes
; Use iSwift:	Yes
; Try disinfect:	No
; Try delete:	No
; Try delete container:	No
; Exclude by mask:	No
; Include by mask:	No
; Objects to scan:	
; 	"C:\virus-checker\healthy.zip"	Enable = Yes	Recursive = No
; ------------------
2019-06-28 09:47:32	Scan_Objects$0544         starting   1%         
2019-06-28 09:47:32	Scan_Objects$0544         running    1%         
2019-06-28 09:47:33 	C:\virus-checker\healthy.zip	archive	ZIP
2019-06-28 09:47:33	C:\virus-checker\healthy.zip//healthy1.txt	ok
2019-06-28 09:47:33	C:\virus-checker\healthy.zip//healthy2.txt	ok
2019-06-28 09:47:43	Scan_Objects$0544         completed             
;  --- Statistics ---
; Time Start:	2019-06-28 09:47:32
; Time Finish:	2019-06-28 09:47:43
; Processed objects:	2
; Total OK:	2
; Total detected:	0
; Suspicions:	0
; Total skipped:	0
; Password protected:	0
; Corrupted:	0
; Errors:	0
;  ------------------
```

Now there are two lines in the report that interest us.
```text
2019-06-28 09:47:33	C:\virus-checker\healthy.zip//healthy1.txt	ok
2019-06-28 09:47:33	C:\virus-checker\healthy.zip//healthy2.txt	ok
```

Notice that report in `C:\virus-checker\healthy-zip-report.txt` contains reports of two scanned 
files, in our case `healthy1.txt` and `healthy2.txt` as if they were a separate files not contained 
in a *zip* archive. 

#### 2.1.3 Scanning an "infected" archive

Create a copy of `healthy.zip`, name it `eicar.zip` and add the testing file `eicar.com` into 
`eicar.zip`.
Run the scanner as usual

```bash
avp.com scan C:\virus-checker\eicar.zip /RA:C:\virus-checker\eicar-zip-report.txt
```
Now open the `C:\virus-checker\eicar-zip-report.txt`.

```text
; --- Settings ---
; Action on detect:	Disinfect automatically
; Scan objects:	All objects
; Use iChecker:	Yes
; Use iSwift:	Yes
; Try disinfect:	No
; Try delete:	No
; Try delete container:	No
; Exclude by mask:	No
; Include by mask:	No
; Objects to scan:	
; 	"C:\virus-checker\eicar.zip"	Enable = Yes	Recursive = No
; ------------------
2019-06-28 09:47:32	Scan_Objects$0544         starting   1%         
2019-06-28 09:47:32	Scan_Objects$0544         running    1%         
2019-06-28 09:47:33 	C:\virus-checker\healthy.zip	archive	ZIP
2019-06-28 09:47:33	C:\virus-checker\eicar.zip//healthy1.txt	ok
2019-06-28 09:47:33	C:\virus-checker\eicar.zip//healthy2.txt	ok
2019-06-28 09:47:33 C:\virus-checker\eicar.zip//eicar.com 	detected	EICAR-Test-File
2019-06-28 09:47:33 C:\virus-checker\eicar.zip//eicar.com 	skipped
2019-06-28 09:47:43	Scan_Objects$0544         completed             
;  --- Statistics ---
; Time Start:	2019-06-28 09:47:32
; Time Finish:	2019-06-28 09:47:43
; Processed objects:	2
; Total OK:	2
; Total detected:	1
; Suspicions:	0
; Total skipped:	1
; Password protected:	0
; Corrupted:	0
; Errors:	0
;  ------------------
```

Notice that report in `C:\virus-checker\eicar-zip-report.txt` contains reports of three scanned 
files that are healthy and one "infected" file - `eicar.com`.
Naturally, the archive is considered to be infected when at least one of its file is infected and 
our future parser needs to be capable of handling this.

### 2.2 Let's do some programming

Now we have enough information to implement parser of Kaspersky command line scanner reports.
We will create a single new Kotlin source file and slightly modify another four source files.

#### 2.2.1 Register the new antivirus

This subsubsection is devoted to almost step-by-step guide on how to implement simple parser for
Kaspersky's reports generated by its command line scanning utility.

Open `viruschecker/driver/src/sk/csirt/viruschecker/driver/config/AntivirusType.kt`.
Its content should look like

```kotlin
package sk.csirt.viruschecker.driver.config

import org.koin.core.qualifier.Qualifier

enum class AntivirusType(
        val antivirusName: String
): Qualifier {
    AVAST("Avast"),
    COMODO("Comodo"),
    ESET("Eset"),
    KASPERSKY("Kaspersky"),
    MICROSOFT("Microsoft"),
    VIRUS_TOTAL("VirusTotal"),
}
```
This file summarizes all supported antivirus software including the VirusTotal which, at least for 
our purposes, can be considered an antivirus (which is not in real life of course).

Although this file already include Kaspersky, we will "re-register" it again, this time as the
*MyKaspersky*, therefore add the `MY_KASPERSKY("MyKaspersky"),` just below the `VIRUS_TOTAL("VirusTotal"),`.

The file should now look like this.

```kotlin
package sk.csirt.viruschecker.driver.config

import org.koin.core.qualifier.Qualifier

enum class AntivirusType(
        val antivirusName: String
): Qualifier {
    AVAST("Avast"),
    COMODO("Comodo"),
    ESET("Eset"),
    KASPERSKY("Kaspersky"),
    MICROSOFT("Microsoft"),
    VIRUS_TOTAL("VirusTotal"),
    MY_KASPERSKY("MyKaspersky"), // <<<<--------- THIS WAS ADDED <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
}
```

#### 2.2.2 Setup command line command

Open `viruschecker/driver/src/sk/csirt/viruschecker/driver/config/DriverPropertiesFactory.kt` 

Now the content of the file should be

```kotlin
package sk.csirt.viruschecker.driver.config

import org.apache.commons.lang3.SystemUtils
import sk.csirt.viruschecker.config.PropertiesFactory

object Properties {
    const val myKaspersky = "my.kaspersky"  // <<<<--------- THIS WAS ADDED <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    const val avast = "avast"
    const val eset = "eset"
    const val kaspersky = "kaspersky"
    const val microsoft = "microsoft"
    const val comodo = "comodo"
    const val virusTotal = "virustotal.apikey"
}

object DriverPropertiesFactory : PropertiesFactory {

    override val propertiesName = "viruschecker-driver.properties"

    override val defaultProperties by lazy {
        if (SystemUtils.IS_OS_WINDOWS) defaultPropertiesWindows
        else defaultPropertiesUnix
    }

}
```

Open `viruschecker/driver/src/sk/csirt/viruschecker/driver/config/WindowsDefaultProperties.kt` 
(or `viruschecker/driver/src/sk/csirt/viruschecker/driver/config/UnixDefaultProperties.kt` depending
 on OS you intend to run the driver with the new antivirus extension).

This files contains a template for generating `viruschecker-driver.properties` 

We need to create a template for command used to execute Kaspersky antivirus. 
You need to modify the template as shown below

```kotlin
package sk.csirt.viruschecker.driver.config

import sk.csirt.viruschecker.driver.antivirus.RunProgramCommand

internal val defaultPropertiesWindows = """
# Avast
# ==============================================================================

${Properties.avast}=ashCmd.exe ${RunProgramCommand.SCAN_FILE} /_> ${RunProgramCommand.REPORT_FILE}

# Eset
# ==============================================================================

${Properties.eset}=ecls.exe ${RunProgramCommand.SCAN_FILE} /log-file=${RunProgramCommand.REPORT_FILE} /log-all

# Kaspersky
# ==============================================================================

${Properties.kaspersky}=avp.com scan ${RunProgramCommand.SCAN_FILE} /RA:${RunProgramCommand.REPORT_FILE} /i0

# Microsoft
# ==============================================================================

${Properties.microsoft}=MpCmdRun.exe -Scan -ScanType 3 -File ${RunProgramCommand.SCAN_FILE} -DisableRemediation

# VirusTotal
# ==============================================================================

${Properties.virusTotal}=<insert-your-api-key>

# MyKaspersky  // <<<<--------- THIS WAS ADDED <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
# ==============================================================================

${Properties.myKaspersky}=avp.com scan ${RunProgramCommand.SCAN_FILE} /RA:${RunProgramCommand.REPORT_FILE} /i0
"""
```

#### 2.2.3 Create the parser

Open IntelliJ IDEA or, if you do not wish to use an IDE, any file manager and navigate to
```bash
viruschecker/driver/src/sk/csirt/viruschecker/driver/antivirus
```

Create a file called `MyKaspersky.kt` and place there the text below.

```kotlin
package sk.csirt.viruschecker.driver.antivirus 

import org.apache.commons.io.FileUtils
import sk.csirt.viruschecker.driver.config.AntivirusType
import java.io.File
import java.nio.charset.Charset
import sk.csirt.viruschecker.driver.antivirus.ScanStatusResult.*

class MyKaspersky(
    scanCommand: RunProgramCommand 
) : CommandLineAntivirus(scanCommand) { 

    override val antivirusName: String = AntivirusType.MY_KASPERSKY.antivirusName // This what we added to `AntivirusType.kt`

    override suspend fun parseReportFile( // This is a name of the parsing function, it MUST be named exactly as is
        reportFile: File, // This represents the file containing the report generated after running the `scanCommand`
        params: FileScanParameters
    ): Report { 
        val reportLines = FileUtils.readLines(
            reportFile,
            Charset.defaultCharset()
        )
        val linesWithScannedFile = reportLines
            .filterNot { it.startsWith(";") }
            .filter { params.fileToScan.name in it }
        
        val scannedStatuses = linesWithScannedFile.map { line ->
            line.split("\t").let { words ->
                Report(
                    when (words[2].trim()) {
                        "ok" -> ScanStatusResult.OK
                        "detected" -> ScanStatusResult.INFECTED
                        else -> ScanStatusResult.NOT_AVAILABLE
                    },
                    if (words.size > 3) words[3] else "OK"
                )
            }
        }

        return scannedStatuses.maxBy { it.status }!!
    }
}
```

Now we will explain the meaning of each part of this file.

* Package into which this file belongs and some dependencies declaration automatically generated by IDE.
  ```kotlin
  package sk.csirt.viruschecker.driver.antivirus
  
  import org.apache.commons.io.FileUtils
  import sk.csirt.viruschecker.driver.config.AntivirusType
  import java.io.File
  import java.nio.charset.Charset 
  import sk.csirt.viruschecker.driver.antivirus.ScanStatusResult.*
  ```

* Header part of our parser. Property `scanCommand` represents the command that will be run. 
  In the case of Kaspersky it is `avp.com scan [SCAN-FILE] /RA:[REPORT-FILE] /i0`.
  ```kotlin
  class MyKaspersky(
      scanCommand: RunProgramCommand 
  ) : CommandLineAntivirus(scanCommand) 
  ```

* Declares the name of this antivirus parser. This is what we added to `AntivirusType.kt`.
    ```kotlin
    override val antivirusName: String = AntivirusType.MY_KASPERSKY.antivirusName // 
    ```
  
* Header part of the main parsing function. 
  This function receives two parameters: 
  * `reportFile` represents the file containing the report generated after running the `scanCommand`
  * `params` represents the information about the scanned file, like its location and the original 
  name of the file that was uploaded by user via the web or cli client interface.
    ```kotlin
      override suspend fun parseReportFile( // This is a name of the parsing function, it MUST be named exactly as is
          reportFile: File, // This represents the file containing the report generated after running the `scanCommand`
          params: FileScanParameters
      ): Report { 
    ```

* Reads all lines from the report file and save them into the local value `reportLines`.
    ```kotlin
    val reportLines = FileUtils.readLines(
        reportFile,
        Charset.defaultCharset()
    )
    ```
* Excludes all lines beginning with `;` character and then returns only lines that contains name of 
  the scanned file.
    ```kotlin
    val linesWithScannedFile = reportLines
        .filterNot { it.startsWith(";") }
        .filter { params.fileToScan.name in it }
    ```

* Map all remaining relevant lines into `Report` structures that will be further
 processed by another application logic.
  * For each line containing the name of the scanned file...
    ```kotlin
    val scannedStatuses = linesWithScannedFile.map { line ->
        ...
    }
    ``` 
  * ...apply `split` operation, that splits each line into list of `words` that were separated by `\t`.
    ```kotlin
    val scannedStatuses = linesWithScannedFile.map { line ->
        line.split("\t").let { words ->
            ...
        }
    }
    ```
    Example: Line `2019-06-28 09:50:18 C:\virus-checker\eicar.com 	detected	EICAR-Test-File`
    will be splitted into words `2019-06-28 09:50:18`, `C:\virus-checker\eicar.com`, `detected` and 
    `EICAR-Test-File` (date and time are separated by standard whitespace and not `\t` which 
    normally looks like (multiple) whitespace(s)).
  
  * For each splitted line we need to create a `Report` structure that contains `status` of the scanned
   file and `malwareDescription` if the scanned file was infected.
    ```kotlin
    val scannedStatuses = linesWithScannedFile.map { line ->
        line.split("\t").let { words ->
            Report(
               status = ...,
               malwareDescription = ...
            )
        }
    }
    ```
    * `status` property of the `Report` is initialized as
      ```kotlin
      status = when (words[2].trim()) {
         "ok" -> OK
         "detected" -> INFECTED
         else -> NOT_AVAILABLE
      }
      ```
      This can be read as follows: If the third word `words[2]` (counted from 0) is equal to `ok` 
      then `status` will by `OK`, else if it is equal to `detected` then the status will be INFECTED.
      Otherwise the status will be `NOT_AVAILABLE1`.
      
    * `malwareDescription` property of the `Report` is initialized as
      ```kotlin
       malwareDescription = if (words.size > 3) words[3] else "OK"
      ```
      This can be read as follows: If the line contains more than 3 words, then the malware 
      description is contained in the fourth word of the line accessed by `words[3]`.
      Otherwise the fourth word is not present and we will just place `"OK"` to the `malwareDescription`.
      
  * If we now try to use the parser to process the report generated by scanning the `eicar.zip`, we will get 
    three parsed `Report` structures. Two of them with status `OK` and one with the status `INFECTED`.
    We want to return just the `Report` with `INFECTED` status.
    
    Statuses are ordered by the natural order being the order in which they are defined, i.e. 
    `NOT_AVAILEBLE` is defined first, `OK` is defined second and `INFECTED` is defined third. 
    Thus, the `NOT_AVAILABLE` status is the "smallest" and the `INFECTED` status is the "largest".
    
    As a subsequence of this we just need the `Report` with the "largest" `status`.
    Therefore the parsing function will finally return  
    ```kotlin
    return scannedStatuses.maxBy { it.status }!!
    ```

We described a relatively simple parser for processing a scan report produced by the Kaspersky 
command line scanning utility.
This implementation is somewhat simplified version of the one provided in 
`viruschecker/driver/src/sk/csirt/viruschecker/driver/antivirus/Kaspersky.kt`.
###### This simplified implementation is not able to report more than one malware in an archive containing more infected files.  

#### 2.2.4 Register parser to the dependency injection  

We also need to register this parser to the dependency injection framework.

Open `viruschecker/driver/src/sk/csirt/viruschecker/driver/config/DependencyInjectionConfig.kt` 

Add the following code 
```kotlin
single<Antivirus>(AntivirusType.MY_KASPERSKY) {
        MyKaspersky(RunProgramCommand(getProperty(Properties.myKaspersky)))
}
```
somewhere in
```kotlin
val driverDependencyInjectionModule = module {
  ...
}
``` 

You should end up with the following content of this file.

```kotlin
package sk.csirt.viruschecker.driver.config

import org.koin.core.qualifier.named
import org.koin.dsl.module
import sk.csirt.viruschecker.driver.antivirus.*
import sk.csirt.viruschecker.driver.parsedArgs

internal val defaultAntivirusQualifier = named("antivirus")

val driverDependencyInjectionModule = module {

    single<Antivirus>(AntivirusType.AVAST) {
        Avast(RunProgramCommand(getProperty(Properties.avast)))
    }

    single<Antivirus>(AntivirusType.ESET) {
        Eset(RunProgramCommand(getProperty(Properties.eset)))
    }

    single<Antivirus>(AntivirusType.KASPERSKY) {
        Kaspersky(RunProgramCommand(getProperty(Properties.kaspersky)))
    }

    single<Antivirus>(AntivirusType.MICROSOFT) {
        Microsoft(RunProgramCommand(getProperty(Properties.microsoft)))
    }

    single<Antivirus>(AntivirusType.COMODO) {
        Comodo(RunProgramCommand(getProperty(Properties.comodo)))
    }

    single<Antivirus>(AntivirusType.VIRUS_TOTAL) {
        VirusTotal(getProperty(Properties.virusTotal))
    }

    single<Antivirus>(AntivirusType.MY_KASPERSKY) { // <<<<--------- THIS WAS ADDED <<<<<<<<<<<<<<<<<<<<<<<<<<<
        MyKaspersky(RunProgramCommand(getProperty(Properties.myKaspersky)))
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    single<Antivirus>(defaultAntivirusQualifier) {
        val antivirusTypesToLoad = parsedArgs.antivirusTypes
        if (antivirusTypesToLoad.size == 1) {
            get(antivirusTypesToLoad[0])
        } else {
            ComposedAntivirus(
                antiviruses = antivirusTypesToLoad.map { get<Antivirus>(it) }
            )
        }
    }

}
```

### 2.4 Run the driver

If all previous steps are completed, then you should be able to run the antivirus with the new 
reimplemented Kaspersky plugin as
```bash
java -jar <name-of-driver> MY_KASPERSKY
```
or enable all recognizable AVs plugins with
```bash
java -jar <name-of-driver> -a
```

Analogously it is possible to add support for any antivirus that comes shipped with the command line
scanning utility.

TODO (Add support for manually updatable AVs) 



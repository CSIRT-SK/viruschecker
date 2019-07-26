Virus Checker
=============

This is a repository for an offline based virus checker application stack, similar to VirusTotal.

It contains several executable modules:

- **driver** program provides REST API to communicate with some antivirus software installed 
on the same machine.
- **gateway** serves to upload files to several *driver*s in parallel. 
- **client-cli**: This is a simple console REST client. One can use it to upload files to gateway.  
- **client-web**: This is a simple graphical web frontend with the same purpose as **client-cli**.

There are also some helper modules that contains shared dependencies or classes.

- **common**
- **cli-common**

The architecture of this software solution is visualized below.

```dtd
                               :---> driver (e.g. Avast)
client-cli ---:                :---> driver (e.g. Eset)
              :---> gateway ---:
client-web ---:                :      ...
                               :---> driver (e.g. Kaspersky)
```

Before diving further we denote the following terms:
- AV = Antivirus software
- JDK = Java Development Kit
- JRE = Java Runtime Environment
- VM = Virtual Machine

Installation
============

These steps describe how to build and deploy this program from scratch and they can be summarized
 in the following steps.

1. Compile the source code
2. Deploy antivirus driver programs
   - Create container/virtual machine for each different antivirus.
   - Install and configure each antivirus
   - Deploy and configure driver program for each antivirus.
3. Deploy sk.csirt.gateway application.
4. Deploy web client application.
5. (Optional) Deploy console client application.

The following subsections thoroughly describe each of the five steps. 

### Installation of prerequisites

As of this moment, the project depends on external VirusTotal API client [library](https://github.com/kdkanishka/Virustotal-Public-API-V2.0-Client).
This library is not provided in public artifact repositories like MavenCentral or JCenter.
Therefore it needs to be compiled and linked manually.
To do this install *Maven* and then execute the following commands.

```bash
git clone https://github.com/kdkanishka/Virustotal-Public-API-V2.0-Client.git
cd Virustotal-Public-API-V2.0-Client
mvn clean install -DskipTests
```

In the future we aim to implement our own client.

1 Compiling the source code
---------------------------

To build this software a JDK 1.8 is required (OpenJDK is sufficient).
The newer JDK versions should work as well, but it was not tested.

Open terminal in this directory and
- on Windows machine run
    ```bash 	
    gradlew.bat clean build shadowJar
    ```
- on Linux machine run
    ```bash 	
    ./gradlew clean build shadowJar
    ```
    
2 Deploy antivirus driver program
---------------------------------

The driver provides a unified REST API to simplify communication with supported antivirus solutions.
As of this moment the supported antivirus software includes
- Avast
- Comodo
- Eset
- Kaspersky
- Windows defender

In addition to this, the driver also supports VirusTotal online virus database.
However, only SHA-256 hashes are uploaded to VirusTotal.

The location of the compiled Java executable is `driver/build/libs/driver-[VERSION]-all.jar`.

In the following subsections we provide a step-by-step instructions for successful deployment.

The recommended setup for the whole Virus Checker system is to have each antivirus and it's 
corresponding driver program installed on separated virtual machine or container.

We provide documentation for two ways of deploying driver programs - on Windows based virtual 
machines using Virtual Box or on Linux based Docker containers.
The first way allows you to reuse some spare licenses for Windows or antivirus solutions.
The other way is usually less demanding on computer resources.

* Deploying on [Windows virtual machine using VirtualBox](docs/driver/drivers-on-windows.md)

* Deploying on [Linux virtual machine using VirtualBox](docs/driver/drivers-on-linux.md)

* Deploying on [Linux containers using Docker](docs/driver/drivers-on-docker.md)

* Enable VirusTotal hash database [guide](docs/driver/driver-virustotal.md)

Of course nothing restricts you to deploy some drivers on Windows and other on Docker.
Or you can deploy driver on any other platform which supports JRE 8 and at least one of the 
supported antivirus.

### 2.1 Run the driver program

The location of the compiled Java executable is `driver/build/libs/driver-[VERSION]-all.jar`.
We will assume that this executable file was copied to `~/virus-checker` or `C:\virus-checker` folder on the virtual 
machine.
 
* Open terminal   

* Type `cd virus-checker`.  

* Type `java -jar [NAME-OF-PROGRAM] [ANTIVIRUS]` and press enter.
    * `[NAME-OF-PROGRAM]` is the name of the driver program.
    * `[ANTIVIRUS]` must be one of the following: `--comodo`. (TODO: 
     auto-detection of the installed antivirus)
        * Concrete example of the above command may be `java -jar driver-1.0.0-all.jar --eset`

To test the successful launch of the driver program open the web browser and go to 
`http://127.0.0.1:8080/`.
The driver should respond with JSON containing some basic info about itself.
More info about the web API can be found in the following subsection. 

### 2.1 Driver REST API

One can communicate with the driver using its REST web API.
You can use it directly or with the provided gateway or client applications.

The API endpoints are documented [here](docs/rest-api/rest-api.md)

3 Deploy gateway
----------------

The purpose of the gateway is to simplify the implementation of client applications.
It receives data from the client and then sends it to all deployed drivers in parallel.

Third party clients can either use the unified gateway API or upload files directly to the drivers.    

Gateway can be theoretically deployed on any machine with Java 8 JRE installed.
However, it was tested only on Ubuntu 18.04.

The location of the compiled JRE executable is `gateway/build/libs/gateway-[VERSION]-all.jar`.

Create a new text file and put the full urls of running driver programs, one url per line.
For example, if you have two running drivers on (virtual) machines with IPv4 addresses 
`192.168.1.112` and `192.168.1.115`, the file should look like below.
```dtd
http://192.168.1.112:8080
http://192.168.1.115:8080
```
Save the file as, for example, `driverUrls.txt`.

Assuming Java is in the *Path*, run terminal in this directory.

Type `java -jar gateway-[VERSION]-all.jar driverUrls.txt` and press enter.
    
To test the successful launch of the driver program open the web browser and go 
to `http://127.0.0.1:8080/`.
The driver should respond with JSON containing some basic info about itself.
More info about the web API can be found in the following subsection.

Remember to open port 8080 for TCP if you wish to connect to the gateway from outside. 


    
4 Deploy client cli application
-------------------------------

This client provides simple text based interface to send files to the gateway and export reports to file.
Currently it supports `txt` and `csv` formats.

The location of the compiled JRE executable is `client-cli/build/libs/client-cli-[VERSION]-all.jar`.

Assuming Java is in the *Path*, run terminal in this directory.

Type `java -jar client-cli-[VERSION]-all.jar -h` and press enter.
Successful launch should print this help
```bash
java -jar client-cli-0.14.1-all.jar -h
usage: [-h] URL FILE-TO-SCAN [-t TIMEOUT] [-o OUT]

optional arguments:
  -h, --help          show this help message and exit

  -t TIMEOUT,         Optional: Sets socket timeout in milliseconds. Default
  --timeout TIMEOUT   value is 50000.

  -o OUT, --out OUT   Specify file to store report. Csv format is supported if
                      the filename ends with appropriate postfix.


positional arguments:
  URL                 Gateway url.

  FILE-TO-SCAN        File to scan. Does not support directories (only
                      archived).
``` 

###### Example

Assume we want to scan the file `eicar.exe` located in the same directory as the client executable, the url of 
running gateway is `http://192.168.1.110`.
Also we want to save the report as `scanReport.csv`.
The command to do this is `java -jar client-cli-[VERSION]-all.jar http://192.168.1.110 eicar.exe --out scanReport.csv`.


5 Deploy client web application
-------------------------------

This client provides simple web based interface to send files to the gateway and show retrieved reports.

The location of the compiled JRE executable is `client-web/build/libs/client-web-[VERSION]-all.jar`.

Assuming Java is in the *Path* and the gateway is running on the same machine, run terminal in this directory.

Type `java -jar client-web-[VERSION]-all.jar http://localhost:8080` and press enter.
By default, the web application will run on port `7979`.

Open the web browser and go to `http://localhost:7979/`.
If the application started successfully you should see a graphical web interface.



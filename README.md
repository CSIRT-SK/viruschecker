Virus Checker
=============

This repository contains an offline based virus checker application stack, similar to VirusTotal.

It contains several executable modules:

- **driver** program provides REST API to communicate with some antivirus software installed 
on the same machine or with the external services.
- **gateway** serves to upload files to several *driver* instances in parallel. 
- **client-cli** is a simple console REST client to upload files to gateway and export the reports to `csv` files.  
- **client-web** is a simple graphical web frontend with the same purpose as **client-cli**.

There are also some helper modules that contains shared dependencies or classes.

- **common**
- **cli-common**

The architecture of this software solution is visualized below.

```dtd
                               :---> driver (e.g. Avast)
client-cli ---:                :---> driver (e.g. Eset, Microsoft, ...)
              :---> gateway ---:
client-web ---:                :      ...
                               :---> driver (e.g. Kaspersky)
```

Before diving further we denote the following terms:
- AV = Antivirus software
- JDK = Java Development Kit
- JRE = Java Runtime Environment
- VM = Virtual Machine
- VM/C = Virtual Machine or Container

Installation
============

First of all, currently we do not provide an one-click/one-command installer.
Also this software does not include a ready-to-use VM/Cs with deployed AVs.

These steps describe how to build and deploy this program from scratch.
They can be summarized in the following steps.

1. Compile the source code
2. Deploy antivirus driver programs
   - Create a VM with supported antivirus programs.
   - Install and configure each antivirus - mainly disable the automatic protections.
   - Run the driver program.
3. Deploy gateway application.
4. Deploy web client application.
5. (Optional) Run console client application.

The following subsections thoroughly describe each of the five steps. 

1 Compiling the source code
---------------------------

To build this software a JDK 1.8 or newer is required (OpenJDK is sufficient).

Open terminal in the project directory and
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

The location of the compiled Java executable is `driver/build/libs/driver-[VERSION]-all.jar`.

Running driver performs these tasks in the following order:
1. Receive a file.
2. Invoke the AV command line tools to scan the file.
3. Read and process the scan reports.
4. Send back the scan result.
  
The driver provides a unified REST API to simplify communication with supported antivirus solutions.
As of this moment the supported antivirus software includes
- Avast
- Comodo
- Eset
- Kaspersky
- Windows defender

In addition to those self hosted services, the driver also supports querying VirusTotal online virus
 database with SHA-256 hashes computed automatically from the scanned file.
Note that the driver uploads only hash of the file to VirusTotal and never the file itself.

### 2.1 Prepare a virtual machine

The recommended setup for the whole Virus Checker system is to have all AVs and the drivers 
installed on one or more AV with forwarded port 8080 to the host.

We provide documentation for two ways of deploying driver programs - on Windows based virtual 
machines or on Linux based virtual machines.
We assume that the VirtualBox is used as a virtualization platform.

* Create a [Windows virtual machine using VirtualBox](docs/driver/drivers-on-windows.md)

* Create a [Linux virtual machine using VirtualBox](docs/driver/drivers-on-linux.md)

* (TODO) Create a [Linux containers using Docker](docs/driver/drivers-on-docker.md)

* Enable VirusTotal hash database [guide](docs/driver/driver-virustotal.md)

As of this moment, the commercial antivirus programs are supported only on Windows, while the free 
one(s) are supported only on Linux.
If you want to run some commercial antivirus like Eset on Linux or implement your own driver for a 
currently unsupported antivirus you may visit this [guide](docs/driver/extensions.md).

### 2.2 Run the deployed driver

The location of the compiled Java executable is `driver/build/libs/driver-[VERSION]-all.jar`.
Copy that file to `~/virus-checker` (on Linux) or 
`C:\virus-checker` (on Windows) folder on the VM.
 
* On the VM, open terminal in the folder with the driver executable.   

* Type `java -jar [NAME-OF-PROGRAM] [ANTIVIRUSES]` and press enter.
    * `[NAME-OF-PROGRAM]` is the name of the driver program.
    * `[ANTIVIRUSES]` must be one or more of the following: 
    `AVAST, COMODO, ESET, KASPERSKY, MICROSOFT, VIRUS_TOTAL`. (TODO: auto-detection of the installed
     antivirus)

* Examples:
    * `java -jar driver-1.0.0-all.jar KASPERSKY` if you have only Kaspersky Antivirus installed on 
    this virtual machine.
    * `java -jar driver-1.0.0-all.jar ESET KASPESRY VIRUS_TOTAL` if you have both Eset and Kaspersky
     installed on this virtual machine and also want to use the VirusTotal service.

To test the successful launch of the driver program open the web browser on the guest and go to 
`http://127.0.0.1:8080/`.
The driver should respond with JSON containing some basic info about itself.
More info about the web API can be found in the following subsection.

If the network adapter of your running VMs have been attached to a NAT with forwarded guest port 
8080, then you can visit `http://127.0.0.1:<insert-forwarded-port>/` on the host as well.   

###### Different port
You may specify the listening port other than **8080** with the `-port=` parameter, e.g. 
`java -jar driver-1.0.0-all.jar ESET COMODO -port=9595` will set the listening port to **9595**.
Please be aware, that ports **7978** and **7979** are reserved by default for other modules of the 
VirusChecker. 
Also you need to re-set port forwarding for the new port instead of **8080**. 

### 2.3 Driver REST API

One can directly communicate with the driver directly using its REST web API.
The API endpoints are documented [here](docs/rest-api/rest-api.md).

### 2.4 Extend driver

If you want to configure the driver or even add support for a new antivirus by yourself, 
this [guide](docs/driver/extensions.md) is the place to go.

3 Deploy gateway
----------------

The location of the compiled JRE executable is `gateway/build/libs/gateway-[VERSION]-all.jar`.

The purpose of the gateway is to simplify the implementation of client applications.
It receives data from the client and then sends it to all deployed drivers in parallel.

Third party clients can either use the unified gateway API or upload files directly to the drivers.    

Gateway can be theoretically deployed on any machine with JRE 1.8.
However, it was tested on Ubuntu 18.04 only.

Create a new text file and put the full urls of running driver programs, one url per line.
For example, if you have two running drivers on VMs with their listening port **8080** forwarded to 
host's ports **8081** and **8082**, the file should look like below.
```dtd
http://localhost:8081
http://localhost:8082
```

Save the file as, for example, `driverUrls.txt`.

Assuming Java is in the *Path*, run terminal in this directory.

Type `java -jar gateway-[VERSION]-all.jar driverUrls.txt` and press enter.
    
To test the successful launch of the driver program open the web browser and go 
to `http://127.0.0.1:8080/`.
The gateway should respond with JSON containing some basic info about itself.
More info about the web API can be found in the following subsection.

Remember to open port 8080 for TCP if you wish to connect to the gateway from other computers in 
network. 

###### Note

The default port of the gateway is 8080 which is also used by the driver program.
If you wish to deploy both the driver and the gateway on the same system/VM, you should change the 
listening port of at least one of them.

Therefore, it is highly recommended that you run the gateway on a different VM (or directly on the 
host) than the driver.

4 Deploy client web application
-------------------------------

This client provides simple web based interface to send files to the gateway and displaying 
retrieved reports.

The location of the compiled JRE executable is `client-web/build/libs/client-web-[VERSION]-all.jar`.

Assuming Java is in the *Path* and the gateway is running on the same machine, run terminal in this 
directory.

Type `java -jar client-web-[VERSION]-all.jar http://localhost:8080` and press enter.
By default, the web application will run on port `7979`.

Open the web browser and go to `http://localhost:7979/`.
If the application started successfully you should see a graphical web interface.

   
5 Deploy client cli application
-------------------------------

This client provides simple text based interface to send files to the gateway and export reports to 
file.
Currently it supports text and `csv` formats.

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

Assume we want to scan the file named `eicar.exe` that you placed in the same directory as the client executable, the url of 
running gateway is, for example `http://192.168.1.110`.
Also we want to save the report as `scanReport.csv`.
We can achieve this by running `java -jar client-cli-[VERSION]-all.jar http://192.168.1.110 eicar.exe --out scanReport.csv`.

Virus Checker
=============

This is a repository for an offline based virus checker application stack, similar to VirusTotal.

It contains several executable modules:

- **driver**: This program provides REST API to communicate with some antivirus software installed 
on the same machine.  
- **client-cli**:  This is a simple console REST client. The client part serves to upload 
files toseveral *driver*s at once and to read/export scan reports.  
- **client-web**: This is a simple graphical web frontend with the same purpose as **client-cli**.

There are also some helper modules that contains shared dependencies or classes.

- **common**
- **cli-common**

The architecture of this software solution is visualized below.

```dtd
                           :---> driver (e.g. Avast)
client-cli ---:            :---> driver (e.g. Eset)
              :------------:
client-web ---:            :      ...
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

1 Compiling the source code
-------------------------

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
- Kaspersky
- Eset

Support for following AVs is under active development

- Avira
- Bitdefender
- Norton
- Windows defender

The location of the compiled JRE executable is `driver/build/libs/driver-[VERSION]-all.jar`.

In the following subsections we provide a step-by-step instructions for successful deployment.

### 2.1 Create virtual machines

The recommended setup for the whole Virus Checker system is to have each antivirus and it's 
corresponding driver program installed in separated virtual machines.

Currently we officially support Windows based virtual machines (VM) using VirtualBox as target 
platform for the driver executable. 
From the technical point of view, however, there is no reason that the Linux based virtual 
machines or even containers (e.g. Docker) would not be sufficient, but we have not tried these 
options yet. 
In future, we aim to provide documentation for this kind of deployment as well.

As mentioned earlier, we will use the Windows based VMs, more specifically the Windows 7 VMs.
Windows 8.1 is, however, more future proof as it has longer support.
Right now we do not recommend Windows 10 because of its forced updates that cannot be completely 
deactivated.

At first, we will install and configure a single VM.
This VM will serve as base VM for cloning, where each clone VM will be separately fitted with 
single antivirus software and our driver executable. 

### 2.2 Install and configure Windows virtual machine

These steps describe how to setup Windows 7 VM in VirtualBox.

#### 2.2.1 Set up virtual machine

* Install some recent version of VirtualBox.

* Create a new machine.

* Type some name, e.g. "VC-Base".

* In the *Memory size* dialog, type 1536 MB. Remember that later we will run 5+ such machines. 

* In the following dialogs, just leave the recommended options.

The virtual machine is now created, however, we need to specify a few additional options like 
networking or location of the installation media.

* Now in *Oracle VM VirtualBox Manager* right click on the newly created VM and select *Settings*.

* Navigate to the *Network* tab and switch the *Attached to* option from *NAT* to *Bridged 
Adapter*. 

* Navigate to the *Storage* tab. 
    * In the *Storage Devices* pane click on the CD icon labeled as *Empty*. 
    * In the right-hand side *Attributes* pane click on the similar looking CD icon located to the 
    right of the *Optical Drive* label and select the *Choose the Virtual Optical Disk file* 
    option.  
    * Open your Windows iso file and close the *Settings* window by clicking on the *Ok* button.
    

#### 2.2.2 Install Windows on the virtual machine

* Start the newly created VM and install the operating system.

* Disable all Windows updates. On Windows 7, this can be done just before the end of the 
installation process.

* Install the *Guest Additions*. 
    * On the topside of the running virtual machine window, click on *Devices* menu and press the 
    *Insert Guest Additions CD Image*.
    * Open the virtual CD Drive, available in *Computer* as a *D:* drive
    * Run the *VBoxWindowsAdditions.exe* and follow the installation steps.
    * After restarting the virtual machine, click on *Devices* top menu item and set *Shared 
    Clipboard* and *Drag and Drop* settings to *Host To Guest*. 


    

* (Optional) For the sake of saving some computer resources, it may be helpful to use the Windows 
Classic or Basic theme. 

##### Setup firewall

Now we need to modify the firewall settings to allow the future deployed *driver executable* to 
communicate with the host operating system. 

* On the virtual machine, press the *Start* button, search the program called "Windows 
Firewall with Advanced Security" and open it.

* In the left-hand side pane choose the *Inbound Rules* option. 

* In the right-hand side panel choose the *New rule* option.

* Choose *Custom* checkbox and press the *Next >* button until you reach the *Scope* pane.
    * Navigate to the *Which remote IP addresses does this rule apply to?* label and choose the 
    *These IP addresses* options.
    * In the large text field bellow write your **host** IP address and press the *Next >*.
    
* When you reach the *Name* pane, type the "VBOX" in the first text field and press the *Finnish*
 button.
 
##### Install Java and copy the driver

The driver application requires Java 8 or newer.
The free and open source Java distributions like AdoptOpenJDK are fully sufficient.

On the **host** system copy the driver Java executable located at 
`driver/build/libs/driver-[VERSION]-all.jar`
to the virtual machine. 
If the Guest Additions are working properly then you may just drag & drop the file from your 
system's file manager to the virtual machine.
Place the *jar* file to some reasonable and easy-to-find location, perhaps `C:\virus-checker`. 

##### Take a snapshot

Now the virtual machine should be prepared to install antivirus software and it's corresponding 
driver application.

Now in the virtual machine topside panel select *Machine* and press *Take snapshot*. 
This will create a backup of the virtual machine in the exactly same state as it is now.
After the snapshot is complete, you may shut down the virtual machine classically using 
*Start*->*Shut down*.

### 2.3 Install and configure antivirus

Driver program supports the aforementioned antivirus solutions.
If you have performed the steps in the previous section, then it is highly recommended to clone the 
virtual machine and install each antivirus on different clone.

##### Clone the machine

In VirtualBox, the virtual machine can be cloned by right-clicking the machine and selecting the 
appropriate option.
Name the clone reasonably, e.g. "VC-Eset" or "VC-Kaspersky" according to the antivirus that will 
be installed on that particular cloned virtual machine. 
Also make sure that the *Reinitialize the MAC address of all network cards* option is **enabled**.

As the driver program will not use the realtime protection provided by the antivirus and it may 
be even harmful for proper functionality of the driver. 
Thus it is highly recommended to disable all realtime protection features except the virus database 
updates.

The following sub-subsections comprise the recommended steps to install and configure each of the
 supported antivirus solutions. 

#### 2.3.1 Avast (paid)

You need the *Avast Pro Antivirus* or *Avast Interner Security*.

This guide assumes the installation of Avast Interner Security, however the other case is similar.
 
##### Configuration

Open the Avast window and click on the *Protection* tab at the left side of the window.
Then click on the, barely visible, cogwheel setup icon at the right side of the window.

In the *Settings* window disable the following features:

* In the *Protection* tab permanently disable the *Core shields*, *Ransomware Shield*, 
*Firewall* and *Real site* features. 

* In the *Privacy* tab, disable the *Sensitive Data Shield*.

* In the *Detection engine* -> *HIPS* tab, make sure the *Enable HIPS* option is disabled. 

* In the *Licensing* window click on the green symbol `x` located to the right of 
*License key* label.

* In the *Firewall* tab, uncheck the *Enable Firewall* option.  

* In the *Web and email* tab, uncheck the *Enable application protocol content filtering* option.  

##### Command line utility

Avast provides the command line utility called *ashCmd.exe* that may be used to scan the 
selected file for malware and to store human readable reports to specified file.

By default, our driver application assumes that the *ashCmd.exe* program is available in the *Path* 
system variable. To do this, follow these steps:

* Press *Start*, search "Computer", right click on the found program with the same name and select 
*Properties* 

* On the left-hand side open the *Advanced system settings*.

* Press the *Environment Variables* button.

* Locate the *Path* variable in the bottom white field labeled as *System variables* and press 
the *Edit...* button below.

* Add the installation path of the Eset to the beginning of *Variable value* text. 
In my case the path is `C:\Program Files\AVAST Software\Avast`.
  
    If you also installed the AdoptOpenJDK 8, the *Variable value* should now look like this. 
    
    ```C:\Program Files\AVAST Software\Avast;C:\Program Files\AdoptOpenJDK\jre-8.0.212.04-hotspot\bin;%SystemRoot%\system32;%SystemRoot%;%SystemRoot%\System32\Wbem;%SYSTEMROOT%\System32\WindowsPowerShell\v1.0\```
    
* A reboot of the virtual machine is now recommended. 

#### 2.3.2 Avira (free)

Download and install the Avira Free Antivirus.

##### Configuration

Open the Avira window and navigate to *Antivirus* item. Click on the `v` symbol at right to the 
*Quick Scan* button and in the popped up menu select *Disable Real-Time Protection* options.


In the *Settings* window disable the following features:

* In the *Protection* tab permanently disable the *Core shields*, *Ransomware Shield*, 
*Firewall* and *Real site* features. 

* In the *Privacy* tab, disable the *Sensitive Data Shield*.

* In the *Detection engine* -> *HIPS* tab, make sure the *Enable HIPS* option is disabled. 

* In the *Licensing* window click on the green symbol `x` located to the right of 
*License key* label.

* In the *Firewall* tab, uncheck the *Enable Firewall* option.  

* In the *Web and email* tab, uncheck the *Enable application protocol content filtering* option.  

##### Command line utility

Avast provides the command line utility called *ashCmd.exe* that may be used to scan the 
selected file for malware and to store human readable reports to specified file.

By default, our driver application assumes that the *ashCmd.exe* program is available in the *Path* 
system variable. To do this, follow these steps:

* Press *Start*, search "Computer", right click on the found program with the same name and select 
*Properties* 

* On the left-hand side open the *Advanced system settings*.

* Press the *Environment Variables* button.

* Locate the *Path* variable in the bottom white field labeled as *System variables* and press 
the *Edit...* button below.

* Add the installation path of the Eset to the beginning of *Variable value* text. 
In my case the path is `C:\Program Files (x86)\Avira\Antivirus`.
  
    If you also installed the AdoptOpenJDK 8, the *Variable value* should now look like this. 
    
    ```C:\Program Files (x86)\Avira\Antivirus;C:\Program Files\AdoptOpenJDK\jre-8.0.212.04-hotspot\bin;%SystemRoot%\system32;%SystemRoot%;%SystemRoot%\System32\Wbem;%SYSTEMROOT%\System32\WindowsPowerShell\v1.0\```
    
* A reboot of the virtual machine is now recommended. 

#### 2.3.3 Bitdefender

(TODO)

#### 2.3.4 Eset (paid)

Install any of the Eset antivirus software, i.e. *Eset Nod32*, *Eset Internet Security*, ... .
This guide assumes the installation of Eset Internet Security, however, in the case of other Eset
 products the steps are basically the same.

##### Configuration

Open the Eset window and click on the setup icon at the left side of the window. 
Disable all features in the *Computer protection*, *Internet protection*, *Network protection* 
and *Security protection* options.

Click on the *Advanced setup* icon at the bottom side of the window, or just press *F5* key.

* In the *Detection engine* -> *Real-time file system protection* tab, uncheck the *Enable 
Real-time file system protection*. 

* In the *Detection engine* -> *Cloud-based protection* tab, uncheck the *Enable ESET 
LiveGrid@ reputation system* and *Enable ESET LiveGrid@ feedback system* options.

* In the *Detection engine* -> *HIPS* tab, make sure the *Enable HIPS* option is disabled. 

* In the *Licensing* window click on the green symbol `x` located to the right of 
*License key* label.

* In the *Firewall* tab, uncheck the *Enable Firewall* option.  

* In the *Web and email* tab, uncheck the *Enable application protocol content filtering* option.  

##### Command line utility

Eset provides the command line utility called *ecls.exe* that may be used to scan the 
selected file for malware and to store human readable reports to specified file.

By default, our driver application assumes that the *ecls.exe* program is available in the *Path* 
system variable. To do this, follow these steps:

* Press *Start*, search "Computer", right click on the found program with the same name and select 
*Properties* 

* On the left-hand side open the *Advanced system settings*.

* Press the *Environment Variables* button.

* Locate the *Path* variable in the bottom white field labeled as *System variables* and press 
the *Edit...* button below.

* Add the installation path of the Eset to the beginning of *Variable value* text. 
In my case the path is `C:\Program Files\ESET\ESET Security`.
  
    If you also installed the AdoptOpenJDK 8, the *Variable value* should now look like this. 
    
    ```C:\Program Files\ESET\ESET Security;C:\Program Files\AdoptOpenJDK\jre-8.0.212.04-hotspot\bin;%SystemRoot%\system32;%SystemRoot%;%SystemRoot%\System32\Wbem;%SYSTEMROOT%\System32\WindowsPowerShell\v1.0\```
    
* A reboot of the virtual machine is now recommended. 

#### 2.3.5 Kaspersky (free)

Install the Kaspersky Free Antivirus. 
If you have the paid version, you can use it as well, but for our purposes the free version is 
sufficient.

##### Configuration

Open the Kaspersky window and click on the settings icon at the bottom of the window. The list of 
features to disable includes:

* In the *General* tab, disable the *Perform recommended actions automatically*

* In the *Protection* tab, disable all options. In the free version the options in this tab are 
  not editable. The workaround around this comprises the following:
    * Press the big red button labeled as *Upgrade protection* 
    and select trial license. 
    * Disable all options in the *Protection* tab.
    * On the bottom of the window click on the green *License* text label. 
    * In the *Licensing* window click on the green symbol `x` located to the right of 
    *License key* label.
    * Then select *Back to Kaspersky Free* option.  
* In the *Additional* tab, do the following:
    * In the *Update* options, check *Do not download new versions...*
    * In the *Reports and Quarantine settings* uncheck the *Store reports no longer than* 
        checkbox.

##### Command line utility

Kaspersky provides the command line utility called *avp.com* that may be used to scan the 
selected file or directory for malware and to store human readable reports to specified file.
It can be also used to perform manual virus database updates.

By default, our driver application assumes that the *avp.com* program is available in the *Path* 
system variable. To do this, follow these steps:

* Press *Start*, search "Computer", right click on the found program with the same name and select 
*Properties* 

* On the left-hand side open the *Advanced system settings*.

* Press the *Environment Variables* button.

* Locate the *Path* variable in the bottom white field labeled as *System variables* and press 
the *Edit...* button below.

* Add the installation path of the Eset to the beginning of *Variable value* text. 
  In my case the path is `C:\Program Files (x86)\Kaspersky Lab\Kaspersky Free 19.0.0`
    
    If you also installed the AdoptOpenJDK 8, the *Variable value* should now look like this. 
    
    ```C:\Program Files (x86)\Kaspersky Lab\Kaspersky Free 19.0.0;C:\Program Files\AdoptOpenJDK\jre-8.0.212.04-hotspot\bin;%SystemRoot%\system32;%SystemRoot%;%SystemRoot%\System32\Wbem;%SYSTEMROOT%\System32\WindowsPowerShell\v1.0\```
    
* A reboot of the virtual machine is now recommended. 

#### 2.3.6 Norton (paid)

/** WIP

Install any of the Norton paid antivirus offer.
This guide assumes the installation of Norton Internet Security, however, another 
Norton products should be basically the same.

Mention should be made that Norton requires updated Windows, otherwise the installation wizard 
refuses to start.
In my case three iterations of Windows 7 updates were sufficient to successfully install the 
antivirus.

##### Configuration

Open the Eset window and click on the setup icon at the left side of the window. 
Disable all features in the *Computer protection*, *Internet protection*, *Network protection* 
and *Security protection* options.

Click on the *Advanced setup* icon at the bottom side of the window, or just press *F5* key.

* In the *Detection engine* -> *Real-time file system protection* tab, uncheck the *Enable 
Real-time file system protection*. 

* In the *Detection engine* -> *Cloud-based protection* tab, uncheck the *Enable ESET 
LiveGrid@ reputation system* and *Enable ESET LiveGrid@ feedback system* options.

* In the *Detection engine* -> *HIPS* tab, make sure the *Enable HIPS* option is disabled. 

* In the *Licensing* window click on the green symbol `x` located to the right of 
*License key* label.

* In the *Firewall* tab, uncheck the *Enable Firewall* option.  

* In the *Web and email* tab, uncheck the *Enable application protocol content filtering* option.  

##### Command line utility

Eset provides the command line utility called *ecls.exe* that may be used to scan the 
selected file for malware and to store human readable reports to specified file.

By default, our driver application assumes that the *ecls.exe* program is available in the *Path* 
system variable. To do this, follow these steps:

* Press *Start*, search "Computer", right click on the found program with the same name and select 
*Properties* 

* On the left-hand side open the *Advanced system settings*.

* Press the *Environment Variables* button.

* Locate the *Path* variable in the bottom white field labeled as *System variables* and press 
the *Edit...* button below.

* Add the installation path of the Eset to the beginning of *Variable value* text. 
In my case the path is `C:\Program Files\ESET\ESET Security`.
  
    If you also installed the AdoptOpenJDK 8, the *Variable value* should now look like this. 
    
    ```C:\Program Files\ESET\ESET Security;C:\Program Files\AdoptOpenJDK\jre-8.0.212.04-hotspot\bin;%SystemRoot%\system32;%SystemRoot%;%SystemRoot%\System32\Wbem;%SYSTEMROOT%\System32\WindowsPowerShell\v1.0\```
    
* A reboot of the virtual machine is now recommended. 

**/

#### 2.3.7 Windows defender (free)

(TODO)

### 2.4 Run the driver program.

We will assume that the driver program was placed at `C:\virus-checker` folder.
 
* Press *Start* button, search and open the "Windows Powershell"   

* Type `cd C:\virus-checker`. You can copy the command and in the blue Powershell window paste it
 with right mouse click (if on Windows 7, the newer versions have much better Powershell terminal). 

* Type `java -jar [NAME-OF-PROGRAM] [ANTIVIRUS]` and press enter.
    * `[NAME-OF-PROGRAM]` is the name of the driver program.
    * `[ANTIVIRUS]` must be one of the following: `--avast`,
     `--avira`, `--bitdefender`, `--eset`, `--kaspersky`, `--norton`, `--windefender`. (TODO: 
     auto-detection of the installed antivirus)
        * Concrete example of the above command may be `java -jar driver-1.0.0-all.jar --kaspersky`

To test the successful launch of the driver program open the web browser and go to 
`http://127.0.0.1:8080/`.
If the "HELLO WORLD" is displayed, the application should be working correctly.

### 2.5 Driver REST API

Driver provides REST web API.
You can use it directly or with the provided client applications.

This subsection describes the API endpoints along with the corresponding requests and responses.






3 Deploy client applications
----------------------------

If at least one driver application is successfully deployed you can its API to query   



4 Deploy client web application
-------------------------------





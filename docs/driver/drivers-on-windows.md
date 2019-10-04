Deploy driver program on Windows based virtual machines 
=======================================================

We will use the Windows based VM, more specifically the Windows 7 VMs.
Windows 8.1 is, however, more future proof as it has longer support.
Right now we do not recommend to use Windows 10 because of its forced updates that cannot be completely 
deactivated.

1 Create virtual machines
-------------------------

These steps describe how to setup Windows 7 VM in VirtualBox.

### 1.1 Set up virtual machine

* Install some recent version of VirtualBox.

* Create a new machine.

* Type some name, e.g. **VC-Windows**.

* Make sure to create a virtual hard drive when asked to (should be the default option). 

* In the *Memory size* dialog, type at least 4096 MB. 

* In the following dialogs, just leave the recommended options.

The virtual machine is now created, however, we need to specify a few additional options like 
networking or location of the installation media.

* Now in the *Oracle VM VirtualBox Manager* right click on the newly created VM and select *Settings*.

* Navigate to the *Network* tab.
    * Make sure the *Enable Network Adapter* is checked.
    * Expand the *Advanced* options and press the *Port Forwarding* button.
    * Click on the green `+` icon at the top right corner of the newly opened window.
    * A port forwarding rule should be created. Now we need to configure it.
        * Double click on **Rule 1** under the *Name* tab and and rename it, perhaps, to **Driver rule**.
        * Analogously change the **0** under the *Host Port* to **8081**.
        * Finally change the **0** under the *Guest Port* to **8080** and press *OK*.
        
* Navigate to the *Storage* tab. 
    * In the *Storage Devices* pane click on the CD icon labeled as *Empty*. 
    * In the right-hand side *Attributes* pane click on the similar looking CD icon located to the 
    right of the *Optical Drive* label and select the *Choose the Virtual Optical Disk file* 
    option.  
    * Open your Windows iso file and close the *Settings* window by clicking on the *OK* button.
    
### 1.2 Install Windows on the virtual machine

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
    
* (Optional) For the sake of saving some VM resources, it may be helpful to use the Windows 
Classic or Basic theme. 

#### 1.2.1 Setup firewall

Now we need to modify the firewall settings to allow the future deployed *driver* executable to 
communicate with the host operating system. 

* On the virtual machine, press the *Start* button, search the program called "Windows 
Firewall with Advanced Security" and open it.

* In the left-hand side pane choose the *Inbound Rules* option. 

* In the right-hand side panel choose the *New rule* option.

* Choose *Custom* checkbox and press the *Next >* button until you reach the *Scope* pane.
    * Navigate to the *Which remote IP addresses does this rule apply to?* label and choose the 
    *These IP addresses* options.
    * In the large text field bellow write your **host** IP address and press the *Next >*.
    
* When you reach the *Name* pane, type the **VBOX** in the first text field and press the *Finnish*
 button.
 
#### 1.2.2 Install Java and copy the driver

The driver application requires JRE 1.8 or newer.
The free and open source Java distributions like AdoptOpenJDK are fully sufficient.

Copy the driver executable *jar* file from  **host** located at 
`.../driver/build/libs/driver-[VERSION]-all.jar`
to the virtual machine. 
If the Guest Additions are working properly then you may just drag & drop the file from your 
system's file manager to the virtual machine.
Place the *jar* file to some reasonable and easy-to-find location, perhaps `C:\virus-checker`. 

#### 1.2.3 (Optional) Take a snapshot

Now the virtual machine should be prepared to install the supported AVs.

In the virtual machine topside panel select *Machine* and press *Take snapshot*. 
This will create a backup of the virtual machine in the exactly same state as it is now.
After the snapshot is complete, you may shut down the virtual machine classically using 
*Start*->*Shut down*.

2 Install and configure antivirus
---------------------------------

Driver program currently supports the aforementioned AVs on Windows: Avast, Eset, Kaspersky, Microsoft.

### 2.1 Avast (paid)

You need the *Avast Pro Antivirus* or *Avast Interner Security*.

This guide assumes the installation of Avast Interner Security, however the other case is basically 
identical.

Optional: After launching the installer, click on *Customize* button and disable all components 
except the *File Shield*.

#### 2.1.1 Configuration

Open the Avast window and click on the *Protection* tab at the left side of the window.
Then click on the, barely visible, cogwheel setup icon at the right side of the window.

In the *Settings* window disable the following features:

* In the *General* -> *Update* navigate to the *Your application is up to date*, click on 
*More options* and select the *Manual update* option. This will disable the automatic application 
updates and keep only virus definitions to be updated.   

* In the *Protection* tab permanently disable the *Core shields*, *Ransomware Shield*, 
*Firewall* and *Real site* features if they were. 

* In the *Privacy* tab, disable the *Sensitive Data Shield*.

* In the *Detection engine* -> *HIPS* tab, make sure the *Enable HIPS* option is disabled. 

* In the *Licensing* window click on the green symbol `x` located to the right of 
*License key* label.

* In the *Firewall* tab, uncheck the *Enable Firewall* option.  

* In the *Web and email* tab, uncheck the *Enable application protocol content filtering* option.  

#### 2.1.2 Command line utility

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

* Add the installation path of the Avast to the beginning of *Variable value* text. 
In my case the path is `C:\Program Files\AVAST Software\Avast`.
  
    If you also installed the AdoptOpenJDK 8, the *Variable value* should now look like this. 
    
    ```C:\Program Files\AVAST Software\Avast;C:\Program Files\AdoptOpenJDK\jre-8.0.212.04-hotspot\bin;%SystemRoot%\system32;%SystemRoot%;%SystemRoot%\System32\Wbem;%SYSTEMROOT%\System32\WindowsPowerShell\v1.0\```
    
### 2.2 Eset (paid)

Install any of the Eset antivirus software, i.e. *Eset Nod32*, *Eset Internet Security*, ... .
This guide assumes the installation of Eset Internet Security, however, in the case of other Eset
 products the steps are basically identical.

#### 2.2.1 Configuration

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

#### 2.2.2 Command line utility

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
    
### 2.3 Kaspersky (free)

Install the Kaspersky Free Antivirus, now called the *Kaspersky Security Cloud*. 
You may also use the paid version, but for our purposes the paid version does not bring any 
benefits.

#### 2.3.1 Configuration

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

#### 2.3.2 Command line utility

Kaspersky provides the command line utility called *avp.com* that may be used to scan the 
selected file or directory for malware and to store human readable reports to specified file.
It can also update the virus database.

By default, our driver application assumes that the *avp.com* program is available in the *Path* 
system variable. To do this, follow these steps:

* Press *Start*, search "Computer", right click on the found program with the same name and select 
*Properties* 

* On the left-hand side open the *Advanced system settings*.

* Press the *Environment Variables* button.

* Locate the *Path* variable in the bottom white field labeled as *System variables* and press 
the *Edit...* button below.

* Add the installation path of the Eset to the beginning of *Variable value* text. 
  Example path is `C:\Program Files (x86)\Kaspersky Lab\Kaspersky Security Cloud 20.0`
    
    If you also installed the AdoptOpenJDK 8, the *Variable value* should now look like this. 
    
    ```C:\Program Files (x86)\Kaspersky Lab\Kaspersky Security Cloud 20.0;C:\Program Files\AdoptOpenJDK\jre-8.0.212.04-hotspot\bin;%SystemRoot%\system32;%SystemRoot%;%SystemRoot%\System32\Wbem;%SYSTEMROOT%\System32\WindowsPowerShell\v1.0\```
    
### 2.4 Windows Defender/Microsoft Security Essentials (free)

Windows 8, 8,1 and 10 contains a quite capable antivirus called Windows Defender.

If you have already installed the other antivirus software, the defender will be disabled.
On Windows 10 you can o turn it on again by clicking on *Start* -> *Settings* -> *Update & Security*
 -> *Turn on Windows Defender*. 

If you have a Windows 7 VM then you need to install Microsoft Security Essentials as Windows 
Defender on this system is unusable for our purposes.

#### 2.4.1 Configuration

##### A) Microsoft Security Essentials (Windows 7)

Open the antivirus window and click on the *Settings* tab at the left side of the window.

Disable the following features:

* In the *Scheduled scan* tab, uncheck the *Run a scheduled scan on my PC*. 

* In the *Real-time protection* tab, uncheck the *Turn on real-time protection*.

* In the *MAPS* tab, make sure the *I don't want to join MAPS* option is selected. 

Microsoft Security Essentials tends to enable Windows Update if it has been disabled before.
If this is also your case, than disable Windows Update once again. 

##### B) Windows Defender (Windows 10)

Windows 8 version of defender has different UI, however the idea is similar - to disable all 
automatic protection features except virus database updates.
  
Run the *Windows Security* app from the *start* menu. 

###### If you have already installed another antivirus software, the Defender will be disabled by default. Just click on *Turn on Windows Defender* in Windows Security app. 

Disable the following features:

* In the *Virus & threat protection* tab, turn off the *Tamper protection*. 

* In the *App & browser control* tab, turn off the *Tamper protection*. 

* In the *Real-time protection* tab, select *Off* option for each setting.

(TODO) Disable telemetry

#### 2.4.3 Command line utility

Microsoft provides the command line utility called *MpCmdRun.exe* that may be used to scan the 
selected file for malware.

By default, our driver application assumes that the *MpCmdRun.exe* program is available in the *Path* 
system variable. To do this, follow these steps:

* Press *Start*, search "Computer", right click on the found program with the same name and select 
*Properties* 

* On the left-hand side open the *Advanced system settings*.

* Press the *Environment Variables* button.

* Locate the *Path* variable in the bottom white field labeled as *System variables* and press 
the *Edit...* button below.

##### A) Microsoft Security Essentials (Windows 7)

* Add the installation path of the Microsoft Security Essentials to the beginning of 
*Variable value* text. 
In my case the path is `C:\Program Files\Microsoft Security Client`.
  
    If you also installed the AdoptOpenJDK 8, the *Variable value* should now look like this. 
    
    ```C:\Program Files\Microsoft Security Client;C:\Program Files\AdoptOpenJDK\jre-8.0.212.04-hotspot\bin;%SystemRoot%\system32;%SystemRoot%;%SystemRoot%\System32\Wbem;%SYSTEMROOT%\System32\WindowsPowerShell\v1.0\```
    
* A reboot of the virtual machine is now recommended.

##### B) Windows Defender (Windows 10)

* Add the installation path of the Defender to the beginning of *Variable value* text. 
In my case the path is `C:\Program Files\Microsoft Security Client`.
  
    If you also installed the AdoptOpenJDK 8, the *Variable value* should now look like this. 
    
    ```C:\Program Files\Microsoft Security Client;C:\Program Files\AdoptOpenJDK\jre-8.0.212.04-hotspot\bin;%SystemRoot%\system32;%SystemRoot%;%SystemRoot%\System32\Wbem;%SYSTEMROOT%\System32\WindowsPowerShell\v1.0\```


Deploy driver program on Linux based virtual machines 
=====================================================

We will use the Linux based VMs, more specifically the Ubuntu 18.04 VMs.
Any modern linux distribution is, however, equally suitable.

1 Create virtual machines
-------------------------

At first, we will install and configure a single VM.
This VM will serve as base VM for cloning, where each clone VM will be separately fitted with 
single antivirus software and our driver executable. 

These steps describe how to setup Ubuntu 18.04 VM in VirtualBox.

### 1.1 Set up virtual machine

* Install some recent version of VirtualBox.

* Create a new machine.

* Type some name, e.g. "VCL-Base".

* In the *Memory size* dialog, type 1024 MB. 

* In the following dialogs, just leave the recommended options.

The virtual machine is now created, however, we need to specify a few additional options like 
networking or location of the installation media.

* Now in the *Oracle VM VirtualBox Manager* right click on the newly created VM and select *Settings*.

* Navigate to the *Network* tab and switch the *Attached to* option from *NAT* to *Bridged 
Adapter*. 

* Navigate to the *Storage* tab. 
    * In the *Storage Devices* pane click on the CD icon labeled as *Empty*. 
    * In the right-hand side *Attributes* pane click on the similar looking CD icon located to the 
    right of the *Optical Drive* label and select the *Choose the Virtual Optical Disk file* 
    option.  
    * Open your Windows iso file and close the *Settings* window by clicking on the *Ok* button.
    

### 1.2 Install Linux on the virtual machine

* Start the newly created VM and install the operating system.

* Install the *Guest Additions*. 
    * On the topside of the running virtual machine window, click on *Devices* menu and press the 
    *Insert Guest Additions CD Image*.
    * Open the virtual CD Drive.
    * Open this directory in terminal and install the VBoxLinuxAdditions 
        ```bash
        sudo apt install gcc make perl
        sudo ./VBoxLinuxAdditions.run 
        sudo usermod -aG vboxsf <place-your-username>
        ```
    * After restarting the virtual machine, click on *Devices* top menu item and set *Shared 
    Clipboard* and *Drag and Drop* settings to *Host To Guest*. 

#### 1.2.1 Setup firewall

Now we need to modify the firewall settings to allow the future deployed *driver* executable to 
communicate with the host operating system. 

```bash
sudo ufv allow from <place-host-ip-here> to any port 8080 proto tcp
```
The above command will open port 8080 which is a default port of the driver program's REST API.
 
#### 1.2.2 Install Java and copy the driver

The driver application requires Java 8 or newer.
The free and open source Java distributions like OpenJDK are fully sufficient.

```bash
sudo apt install default-jdk
```

Copy the driver executable *jar* file from  **host** located at 
`.../driver/build/libs/driver-[VERSION]-all.jar`
to the virtual machine. 
If the Guest Additions are working properly then you may just drag & drop the file from your 
system's file manager to the virtual machine.
Place the *jar* file to some reasonable and easy-to-find location, perhaps `~/virus-checker` where 
`~` denotes your home directory. 

#### 1.2.3 Take a snapshot

Now the virtual machine should be prepared to install antivirus software and it's corresponding 
driver application.

Now in the virtual machine topside panel select *Machine* and press *Take snapshot*. 
This will create a backup of the virtual machine in the exactly same state as it is now.
After the snapshot is complete, you may shut down the virtual machine classically using 
*Start*->*Shut down*.

2 Install and configure antivirus
---------------------------------

Driver program supports the aforementioned antivirus solutions.
If you have performed the steps in the previous section, then it is highly recommended to clone the 
virtual machine and install each antivirus on different clone.

#### 2.0.1 Clone the machine

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

### 2.1 Comodo

To install *Comodo Antivirus* open terminal and execute the following commands.

* Install the *libssl* library.
    ```bash
    sudo apt install libssl
    ```
* Download the installation package
    ```bash
    wget http://download.comodo.com/cis/download/installs/linux/cav-linux_x64.deb
    ```
* Install the antivirus
    ```bash
    sudo gdebi -n cav-linux_x64.deb
    ``` 
* Run the post installation script
    ```bash
    sudo /opt/COMODO/post_setup.sh
    ``` 
  
#### 2.1.1 Configuration

Open the Comodo window and click on the *Antivirus* tab at the top side of the window.

* In the *Scheduled Scans* remove the weekly scheduled scan.

* In the *Scanner Settings* disable the *Real-Time Scanning* option.


* In the *Protection* tab permanently disable the *Core shields*, *Ransomware Shield*, 
*Firewall* and *Real site* features. 

* In the *Privacy* tab, disable the *Sensitive Data Shield*.

* In the *Detection engine* -> *HIPS* tab, make sure the *Enable HIPS* option is disabled. 

* In the *Licensing* window click on the green symbol `x` located to the right of 
*License key* label.

* In the *Firewall* tab, uncheck the *Enable Firewall* option.  

* In the *Web and email* tab, uncheck the *Enable application protocol content filtering* option.  

#### 2.1.2 Command line utility

Comodo provides the command line utility called *cmdscan* that may be used to scan the 
selected file for malware.
It should be located at `/opt/COMODO`.
The driver is preconfigured to this location and therefore no additional settings should be 
necessary. 


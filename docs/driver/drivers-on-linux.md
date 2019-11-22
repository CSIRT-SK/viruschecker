Deploy driver program on a Linux based virtual machine 
======================================================

We will use the Linux based VM, more specifically the Ubuntu 18.04 VM.
Any modern linux distribution is, however, equally suitable.

1 Create virtual machine
------------------------

These steps describe how to setup Ubuntu 18.04 VM in VirtualBox.

### 1.1 Set up virtual machine

* Install some recent version of VirtualBox.

* Create a new machine.

* Type some name, e.g. **VC-Linux**.

* In the *Memory size* dialog, type 2048 MB. 

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
        * Analogously change the **0** under the *Host Port* to **8082**.
        * Finally change the **0** under the *Guest Port* to **8080** and press *OK*.

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

#### 1.2.2 Install Java and copy the driver

The driver application requires JRE 8 or newer.
The free and open source Java distributions like OpenJDK are fully sufficient.

```bash
sudo apt install default-jdk
```

Copy the driver executable *jar* file from  **host** located at 
`.../driver/build/libs/driver-[VERSION]-all.jar`
to the virtual machine. 
If the Guest Additions are working properly then you may just drag & drop the file from your 
host system's file manager to the virtual machine.
Place the *jar* file to some reasonable and easy-to-find location on the guest, perhaps `~/virus-checker` where 
`~` denotes your home directory. 

#### 1.2.3 (Optional)  Take a snapshot

Now the virtual machine should be prepared to install antivirus software and it's corresponding 
driver application.

Now in the virtual machine topside panel select *Machine* and press *Take snapshot*. 
This will create a backup of the virtual machine in the exactly same state as it is now.
After the snapshot is complete, you may shut down the virtual machine classically using 
*Start*->*Shut down*.

2 Install and configure antivirus
---------------------------------

Driver program currently supports the aforementioned AVs on Windows: Comodo.

### 2.1 Comodo

###### On Ubuntu 18.04, this AV needs older libssl library than the one shipped with the distribution. This may cause incompatibility with other AVs.
 

To install *Comodo Antivirus* open terminal and execute the following commands.

* Install the *libssl0.9.8* library.
    ```bash
    wget archive.ubuntu.com/ubuntu/pool/universe/o/openssl098/libssl0.9.8_0.9.8o-7ubuntu3.2_amd64.deb	

    sudo dpkg -i libssl0.9.8_0.9.8o-7ubuntu3.2_amd64.deb

    sudo apt-get install -f
    ```

* Install the antivirus
    ```bash
    wget http://download.comodo.com/cis/download/installs/linux/cav-linux_x64.deb

    sudo gdebi -n cav-linux_x64.deb

    sudo /opt/COMODO/post_setup.sh
    ``` 
  
#### 2.1.1 Configuration

Open the Comodo window and click on the *Antivirus* tab at the top side of the window.

* In the *Scheduled Scans* remove the weekly scheduled scan.

* In the *Scanner Settings* disable the *Real-Time Scanning* option.

#### 2.1.2 Command line utility

Comodo provides the command line utility called *cmdscan* that may be used to scan the 
selected file for malware.
It should be located at `/opt/COMODO`.
The driver is preconfigured to this location and therefore no additional settings are required. 


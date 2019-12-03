Deploy gateway program on Linux based virtual machines 
======================================================

We will assume that the gateway will be deployed on a dedicated Linux VM.

* Create a (linked) clone of some VM running the driver or install a new one. 

* Set the network adapter in the VM settings to *Bridged*.

* Allow host ports that are forwarded from the driver's VM to be opened for IP address of the 
gateway's VM. 
Let's assume you have two running drivers on VMs with their listening port **8080** forwarded to 
host's ports **8081** and **8082**.
    * If your host OS is Linux you may use the command bellow in the host's terminal with 
      superuser privileges.
      ```bash
      sudo ufw allow proto tcp from <insert-gateway-VM-IP> to any port 8081,8082
      ```
    * On a Windows host follow these steps.
        * Press the *Start* button, search the program called *Windows Firewall with Advanced Security* (Win 7) 
        or *Windows Defender Firewall with Advanced Security* (Win 10) and open it.
        * In the left-hand side pane choose the *Inbound Rules* option. 
        * In the right-hand side panel choose the *New rule* option.
        * Choose *Custom* checkbox and press the *Next >* button until you reach the *Scope* pane.
            * Navigate to the *Which remote IP addresses does this rule apply to?* label and choose the 
            *These IP addresses* options.
            * In the large text field bellow write IP address of the gateway VM and press the *Next >*.
        * When you reach the *Name* pane, type the **Gateway** in the first text field and press the *Finnish*
        button.

* Copy the gateway JAR executable to the VM.

* In the folder with the copied JAR executable, create a new text file and put the full urls of the 
running drivers. 
For example, if you have two running drivers on VMs with their listening port **8080** forwarded to 
the host ports **8081** and **8082**, the file should look like below.
    ```dtd
    http://<insert-host-IP>:8081
    http://<insert-host-IP>:8082
    ```

* Save the file as, for example, `driverUrls.txt`.

* Assuming Java is installed, run terminal in this directory.

* Type `java -jar gateway-[VERSION]-all.jar driverUrls.txt` and press enter.
    
To verify the successful launch of the gateway, open a web browser in a VM and go 
to `http://127.0.0.1:8080/`.
The gateway should respond with JSON containing some basic info about itself.

Also open ports **8080** if you wish to connect to the gateway from other 
computers on the network including your host OS.
```bash
sudo ufw allow proto tcp from any to any port 8080
```




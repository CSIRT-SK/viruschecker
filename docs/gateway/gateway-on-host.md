Deploy gateway program on host OS 
=================================

We will assume that the gateway will be deployed on the host OS.

###### Note: Windows or MacOS machines with low free ram 

Let's assume you have two running drivers on VMs with their listening port **8080** forwarded to 
host's ports **8081** and **8082**.

* In the folder with the gateway JAR executable, create a new text file and put the full urls of the 
running drivers. 
For example, if you have two running drivers on VMs with their listening port **8080** forwarded to 
the host ports **8081** and **8082**, the file should look like below.
    ```dtd
    http://localhost:8081
    http://localhost:8082
    ```

* Save the file as, for example, `driverUrls.txt`.

* Assuming Java is installed, run terminal in this directory.

* Type `java -jar gateway-[VERSION]-all.jar driverUrls.txt` and press enter.
    
To verify the successful launch of the gateway, open a web browser and go 
to `http://127.0.0.1:8080/`.
The gateway should respond with JSON containing some basic info about itself.

### (Optional) Make gateway accessible from outside

If you wish to connect to the gateway from other computers on the network follow the steps below.

* If your host OS is Linux you may use the command bellow in the host's terminal with 
  superuser privileges.
```bash
sudo ufw allow proto tcp from any to any port 8080
```
* On a Windows host follow these steps.

    * Press the *Start* button, search the program called *Windows Firewall with Advanced Security* 
    (Win 7) or *Windows Defender Firewall with Advanced Security* (Win 10) and open it.

    * In the left-hand side pane choose the *Inbound Rules* option. 

    * In the right-hand side panel choose the *New rule* option.

    * Choose *Port* checkbox and press the *Next >* button.
         * Select the *TCP* and *Specific local ports* with value **8080**
         * Just keep clicking on the *Next >* button.





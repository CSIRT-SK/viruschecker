Modify driver program
=====================

There are two types of modification that are described in this guide.
 
* Modification of commands that are used to launch antivirus scanner 
* Adding support for another antivirus


1 Modification of commands to launch antivirus scanner
------------------------------------------------------

If you run the driver program for the first time, a file named `viruschecker-driver.properties` will
be generated.
Content of the file depends on the operating system.

For example, for driver version `0.18.0` the content will look like this on Windows

```properties

```   

The VirusTotal API requires API key to be set. 
You can obtain the free api key [here](https://support.virustotal.com/hc/en-us/articles/115002088769-Please-give-me-an-API-key).
Be noted that the free api key is limited to 4 requests per minute.
Otherwise you need to purchase the premium api key.

Firstly you need to place your api key into `viruschecker-driver.properties`.
If you do not have this file, then just run the driver program without any arguments like 
```bash
java -jar <name-of-driver>
```

The driver will automatically create the aforementioned file.
Open the file with any text editor that is better than *Notepad*.
In this line 
```bash
virustotal.apikey=<insert-your-api-key>
``` 
replace the `<insert-your-api-key>` with your api key.

2 Run the driver program
------------------------

This mode is OS agnostic and can be run as
```bash
java -jar <name-of-driver> --virustotal
```

If you do not wish to run the driver in this mode in it's own VM/container, but rather alongside 
other instance of the *driver* program or *gateway* then it is necessary to set different port to 
avoid complications due to multiple processes accessing the same port.  

```bash
java -jar <name-of-driver> --virustotal -port=<some-port-other-than-8080-or-7979>
```

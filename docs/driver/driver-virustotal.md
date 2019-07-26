Deploy driver for VirusTotal 
============================

Driver application can be run in a special mode available via `VIRUS_TOTAL` command line parameter.
Instead of scanning the uploaded file via it's REST API, it will calculate the SHA-256 hash of the 
file and check it against the VirusTotal online database.

1 Place the api key
-------------------

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
java -jar <name-of-driver> VIRUS_TOTAL
```

Although driver programs supports serving multiple antivirus programs installed on the same 
(virtual) machine, the `VIRUS_TOTAL` parameter is deliberately incompatible with other antivirus 
parameters and it will cause a runtime exception if `VIRUS_TOTAL` parameter is specified with 
another antivirus. It means that, for example `java -jar <name-of-driver> VIRUS_TOTAL COMODO` will 
instantly crash.

If you do not wish to run the driver in this mode in it's own VM/container, but rather alongside 
with other instance of the *driver* program then it is necessary to set different port to 
avoid complications due to multiple processes accessing the same port.  

Therefore to run for example driver for Comodo and VirusTotal on the same (virtual) machine, 
just run them as two separate processes.

```bash
java -jar <name-of-driver> COMODO
java -jar <name-of-driver> VIRUS_TOTAL -port=<some-port-other-than-8080-or-7979>
```



Also remember to open the port in the firewall.
Firewall configurations for both [Windows](/docs/driver/drivers-on-windows.md) and [Linux](/docs/driver/drivers-on-linux.md) are described in section *1.2.1 Setup firewall*.

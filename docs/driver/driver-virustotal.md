Deploy driver program on Linux based virtual machines 
=====================================================

Driver application can be run in a special mode available via `--virustotal` command line switch.
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
java -jar <name-of-driver> --virustotal
```

If you do not wish to run the driver in this mode in it's own VM/container, but rather alongside 
other instance of the *driver* program or *gateway* then it is necessary to set different port to 
avoid complications due to multiple processes accessing the same port.  

```bash
java -jar <name-of-driver> --virustotal -port=<some-port-other-than-8080-or-7979>
```

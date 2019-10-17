Deploy driver for VirusTotal 
============================

Driver application can be run in a special mode available via `VIRUS_TOTAL` command line parameter.
Instead of scanning the uploaded file via it's REST API, it will calculate the SHA-256 hash of the 
file and check it against the VirusTotal online database.

The VirusTotal API requires API key to be set. 
You can obtain the free api key [here](https://support.virustotal.com/hc/en-us/articles/115002088769-Please-give-me-an-API-key).
Be noted that the free api key is limited to 4 requests per minute.
Otherwise you need to purchase the premium api key.

Firstly you need to place your api key into `viruschecker-driver.properties`.
If you do not have this file, then just run the driver program without any arguments like 
```bash
java -jar <name-of-driver>
```
and shut it down with `Ctrl+C` (or just close the terminal).

The driver will automatically create the aforementioned file.
Open the file with any text editor that is better than *Notepad*.
In this line 
```bash
virustotal.apikey=<insert-your-api-key>
``` 
replace the `<insert-your-api-key>` with your api key.


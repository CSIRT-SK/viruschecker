Deploy driver program on Linux based virtual machines 
=====================================================

Driver application can be run in a special mode available via `--virustotal` command line switch.
Instead of scanning the uploaded file via it's REST API, it will calculate the SHA-256 hash of the 
file and check it against the VirusTotal online database.

This mode is OS agnostic and can be run as
```bash
java -jar <name-of-driver> --virustotal
```

If you do not wish to run the driver in this mode in it's own VM/container, then set different port. 

```bash
java -jar <name-of-driver> --virustotal -port=<some-port>
```

Gateway REST API
================

This section describes the API endpoints along with the corresponding requests and responses.

In request/response body schemas below, all data types are implicitly **not null**. 
Nullable values are explicitly denoted with `?` symbol. 
For example, data type `String` is guaranteed not to be of value `null`, however it still may 
contain empty value denoted by `""`.
On the other hand, data type `String?` may contain `null` value.

Gateway info
------------

### GET `/`

Get basic gateway info.

#### Request body schema 

None

#### Response body schema 

* **200 OK**
    Type: *application/json*
    ``` 
    structure GatewayInfoResponse
        gatewayVersion: String
    ``` 
    Example:
    ``` 
    {
        "gatewayVersion": "0.18.1",
    }
    ```    
    
Drivers info
------------

### GET `/driversInfo`

Get information about the deployed drivers to which this gateway is connected.

#### Request body schema 

None

#### Response body schema 

* **200 OK**

    Type: *application/json*
    
    Returns list of *UrlAntivirusDriverInfoResponse*
    
    *UrlAntivirusDriverInfoResponse schema*
    ``` 
    structure UrlDriverInfoResponse:
        url: String
        success: Boolean   # `true` if connection to driver on [url] was successfull.
        info: AntivirusDriverInfoResponse   
    ```
    ``` 
    structure DriverInfoResponse:
        driverVersion: String
        antivirus: String
    ``` 
  
    Example 1:
    ``` 
    [
        {
            "url: "http://192.168.1.112",
            "success": "true", 
            "info": {
                "driverVersion": "0.18.1",
                "antivirus": "Avast, Eset"
            }
        },
        {
            "url: "http://192.168.1.115",
            "success": "true", 
            "info": {
                "driverVersion": "0.16.0",
                "antivirus": "Comodo"
            }
        },
        {
            "url: "http://192.168.1.118",
            "success": "true" 
            "info": {
                "driverVersion": "0.18.1",
                "antivirus": "VirusTotal"
            }
        },
        {
            "url: "http://192.168.1.121",
            "success": "false", 
            "info": {
                "driverVersion": "ERROR: Could not reach driver.",
                "antivirus": "NA"
            }
        }
    ]
    ```             

Multi scan file
---------------

### POST `/multiScanFile`

Upload file to all deployed drivers in parallel and returns scan report.

#### Request body schema 

Type: *multipart/form-data*

Example: 
```
Content-Type: multipart/form-data; boundary=---------------------------9051914041544843365972754266
Content-Length: ...

-----------------------------9051914041544843365972754266
Content-Disposition: form-data; name="externalDrivers"

false   #(Note: this value determined if the external services like VirusTotal will be used)
-----------------------------9051914041544843365972754266
Content-Disposition: form-data; name="file"; filename="eicar.exe"
```


#### Response body schema 

* **200 OK**

    Type: *application/json*
    ``` 
    structure FileHashScanResponse
        sha256: String
        md5: String
        sha1: String
        report: FileScanResponse
    ``` 
    ```
    structure FileScanResponse:
        date: DateTimeUTC
        filename: String
        status: ScanStatusResponse
        results:  List<AntivirusReportResponse>
    ``` 
    ```
    enumeration ScanStatusResponse:
        SCAN_REFUSED
        NOT_AVAILABLE
        OK
        INFECTED
    ```
    ```
    structure AntivirusReportResponse:
        antivirus: String
        status: ScanStatusResponse
        malwareDescription: String
    ```

    Example:
    ``` 
    {
        "sha256": "275a021bbfb6489e54d471899f7db9d1663fc695ec2fe2a2c4538aabf651fd0f",
        "md5": "44d88612fea8a8f36de82e1278abb02f",
        "sha1": "3395856ce81f2b7382dee72602f798b642f14140",
        "report": {
            "date": "2019-07-17T07:24:23.530Z",
            "filename": "eicar.exe",
            "status": "INFECTED",
            "results": [
                {
                    "antivirus": "Avast",
                    "status": "INFECTED",
                    "malwareDescription": "EICAR Test-NOT virus"
                },
                {
                    "antivirus": "Eset",
                    "status": "INFECTED",
                    "malwareDescription": "Eicar test file"
                },
                {
                      "antivirus": "VirusTotal",
                      "status": "SCAN_REFUSED",
                      "malwareDescription": "The caller did not want to use this external service."
                }
            ]    
        } 
    ```
      
* **400 Bad Request**
    
    If file upload is unsuccessful.
    
### WS `/ws/multiScanFile`

Upload file to all deployed drivers in parallel and returns scan reports via WebSocket.

#### Send 

1.  Type: *frame/text*, JSON format
   
    Arity: 1
    ``` 
    structure ScanFileWebSocketParameters:
        useExternalServices: Boolean
        originalFilename: String
    ``` 

2.  Type: *frame/binary*
   
    Arity: 1
    
#### Receive 

1.  Type: *frame/text*, JSON format

    Arity: 1
    
    ``` 
    structure HashResponse:
        md5: String
        sha1: String
        sha256: String
    ``` 
    
2.  Type: *frame/text*, JSON format
   
    Arity: 1..N
    
    ``` 
    structure AntivirusReportResponse:
        antivirus: String
        status: ScanStatusResponse
        malwareDescription: String
    ``` 
    ```
    enumeration ScanStatusResponse:
        SCAN_REFUSED
        NOT_AVAILABLE
        OK
        INFECTED
    ```
Retrieve scan report
--------------------

### GET  `/scanReport/{sha256}`

Get stored scan report of a file.

**Path parameters** 

`
    sha256: SHA-256 hash of some file.
`

Currently only searching by SHA-256 hashes is supported

#### Response body schema 

* **200 OK** 
    
    (identical with POST /multiScanFile)

* **204 No Content**
    
    If scan report is not found.
    
    
Retrieve all scan reports
-------------------------

### GET  `/allScanReports`

Get stored scan report of a file.

**Path parameters** 

None

#### Response body schema 

* **200 OK** 

    ```List<FileHashScanResponse>```    
    
    *FileHashScanResponse* schema is identical with POST /multiScanFile
    
Search scan reports
-------------------

### GET  `/scanReportBy/{searchWords}`

Get stored scan report of a file.

**Path parameters** 

`
    searchWords: Search words separated by ','.
`

#### Response body schema 

* **200 OK** 

    ```List<FileHashScanResponse>```    
    
    *FileHashScanResponse* schema is identical with POST /multiScanFile
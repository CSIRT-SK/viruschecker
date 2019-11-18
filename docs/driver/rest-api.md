Driver REST API
===============

This section describes the API endpoints along with the corresponding requests and responses.

In request/response body schemas below, all data types are implicitly **not null**. 
Nullable values are explicitly denoted with `?` symbol. 
For example, data type `String` is guaranteed not to be of value `null`, however it still may 
contain empty value denoted by `""`.
On the other hand, data type `String?` may contain `null` value.


Driver info
-----------

### GET `/`

Get basic driver info.

#### Request body schema 

None

#### Response body schema

* **200 OK**

    Type: *application/json*
    ``` 
    structure DriverInfoResponse:
        antivirus: String
        driverVersion: String
    ``` 
    Example 1:
    ``` 
    {
        "antivirus": "Comodo, VirusTotal",
        "driverVersion": "0.18.1",
    }
    ```
    Example 2:
     ``` 
     {
         "antivirus": "Eset, Avast",
         "driverVersion": "0.18.1"
     }
     ```               

Scan file
---------

### POST `/scanFile`

Upload virus file and returns scan report.

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

    Example 1:
    ``` 
    {
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
            }
        ]
    }
    ```

    Example 2:
    ``` 
    {
        "date": "2019-07-17T07:24:23.530Z",
        "filename": "zipWithEicar.zip",
        "status": "INFECTED",
        "results": [
            {
                 "antivirus": "Avast",
                 "status": "OK",
                 "malwareDescription": "OK"
            },
            {
                "antivirus": "Eset",
                "status": "INFECTED",
                "malwareDescription": "Eicar test file"
            }
        ]
    }
    ```
  
* **400 Bad Request**
    
    If file was not received.

### WS `/ws/scanFile`

Upload virus file and returns scan report via WebSocket.

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
       
   
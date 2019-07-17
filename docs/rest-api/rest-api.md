Driver REST API
===============

This subsection describes the API endpoints along with the corresponding requests and responses.

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
    structure AntivirusDriverInfoResponse:
        driverVersion: String,
        antivirus: String
    ``` 
    Example:
    ``` 
    {
        "driverVersion": "0.14.1",
        "antivirus": "Eset"
    }
    ```               

Scan file
---------

### POST `/scanFile`

Upload virus file and returns scan report.

#### Request body schema 

Type: *multipart/form-data*

#### Response body schema

* **200 OK**

    Type: *application/json*
    ``` 
    structure FileScanResponse:
        filename: String,
        antivirus: String,
        status: Status,
        malwareDescription: String
    ``` 

    *Status schema*
    ```
    enumeration Status:
        NOT_AVAILABLE,
        OK,
        INFECTED
    ```

    Example:
    ``` 
    {
        "filename": "eicar.exe",
        "antivirus": "Eset",
        "status": "INFECTED",
        "malwareDescription": "Eicar test file"
    }
    ```

Gateway REST API
================

One can communicate with the driver using its REST web API.
You can use it directly or with the provided gateway or client applications.

The API endpoints are documented [here](docs/rest-api/rest-api.md)

This subsection describes the API endpoints along with the corresponding requests and responses.

As mentioned in subsection 2.5, we distinguish between nullable and non-nullable attributes in 
request/response body schemas where 

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
        "gatewayVersion": "0.14.1",
    }
    ```    

Drivers info
------------

### GET `/driversInfo`

Get information about deployed drivers to which this gateway is connected.

#### Request body schema 

None

#### Response body schema 

* **200 OK**

    Type: *application/json*
    
    Returns list of *UrlAntivirusDriverInfoResponse*
    
    *UrlAntivirusDriverInfoResponse schema*
    ``` 
    structure UrlAntivirusDriverInfoResponse:
        url: String,
        success: Boolean,   // If connection to driver on [url] was successfull.
        info: AntivirusDriverInfoResponse   
    ```
    
    *AntivirusDriverInfoResponse schema*
    ``` 
    structure AntivirusDriverInfoResponse:
        driverVersion: String,
        antivirus: String
    ``` 
    Example 1:
    ``` 
    [
        {
            "url: "http:192.168.1.112",
            "success": "true" 
            "info": {
                "driverVersion": "0.14.1",
                "antivirus": "Avast"
            }
        },
        {
            "url: "http:192.168.1.115",
            "success": "true" 
            "info": {
                "driverVersion": "0.14.1",
                "antivirus": "Eset"
            }
        },
        {
            "url: "http:192.168.1.118",
            "success": "false" 
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

Upload virus file to all deployed drivers in parallel and returns scan report.

#### Request body schema 

Type: *multipart/form-data*

#### Response body schema 

* **200 OK**

    Type: *application/json*
    ``` 
    structure MultiFileScanResponse
        date: DateTimeUTC,
        sha256: String.
        filename: String,
        status: Status,
        otherHashes: List<Hash>,
        reports: List<AntivirusScanResponse>
    ``` 

    *Status schema*
    ```
    enumeration Status:
        NOT_AVAILABLE,
        OK,
        INFECTED
    ```

    *Hash schema*
    ```
    structure Hash:
        value: String,
        algorithm: String
    ```

    *AntivirusScanResponse schema*
    ``` 
    structure AntivirusScanResponse
        antivirus: String,
        status: Status.
        malwareDescription: String
    ``` 

    Example 1:
    ``` 
    {
        "date": "2019-07-17T07:24:23.530Z",
        "sha256": "275a021bbfb6489e54d471899f7db9d1663fc695ec2fe2a2c4538aabf651fd0f".
        "filename": "eicar.exe",
        "status": "INFECTED",
        "otherHashes": [
            {
                "44d88612fea8a8f36de82e1278abb02f",
                "MD5"
            }
        ],
        "reports": [
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
        "sha256": "999722a3258c9a3ff0a680fa6c09b12616db879e6dbb18a828489cf6e49369a1".
        "filename": "zipWithEicar.zip",
        "status": "INFECTED",
        "otherHashes": [
            {
                "1b0df9b117c619475a30dafea0bc20fb",
                "MD5"
            }
        ],
        "reports": [
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
    
    If file upload is unsuccessful.
    

Retrieve scan report
--------------------

### GET   `/scanReport/{sha256}`

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
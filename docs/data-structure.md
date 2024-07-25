# Data Structure

## DynamoDB Schema

The DynamoDB table for the Licenser Module uses the following schema:

- `licenseid` (String, Partition Key): A UUID representing the unique identifier for each license.
- `appurls` (String): A JSON string containing an array of allowed application URLs.
- `changedtimestamp` (Number): Unix timestamp of the last change to the license record.
- `customerid` (String): A UUID representing the unique identifier for the customer.
- `customername` (String): The name of the customer.
- `expirationdate` (String): ISO 8601 formatted date string for license expiration.
- `isrevoked` (Boolean): Indicates whether the license has been revoked.
- `notes` (String): Additional notes or comments about the license.
- `services` (String): A JSON string containing an array of licensed services.

### Example item:

```json
{
  "licenseid": {
    "S": "d5b35ffa-af15-44b7-9de0-5b1b6dc4daec"
  },
  "appurls": {
    "S": "[{\"URL\":\"http://asd.soic\"}]"
  },
  "changedtimestamp": {
    "N": "1718622505782"
  },
  "customerid": {
    "S": "869b100f-06b7-44cc-80df-b4c4bf728461"
  },
  "customername": {
    "S": "Test Customer"
  },
  "expirationdate": {
    "S": "2025-01-03T00:00:00.000Z"
  },
  "isrevoked": {
    "BOOL": false
  },
  "notes": {
    "S": "asdasf"
  },
  "services": {
    "S": "[{\"serviceName\":\"TEST\",\"serviceValue\":\"EmbeddedPowerBI\"}]"
  }
}
```

When inserting or updating items in the DynamoDB table, ensure that you follow this schema structure. The Lambda function (`app.py`) is designed to work with this schema when verifying licenses and generating authorization tokens.


## License key

The license key is a JSON Web Token (JWT) used to identify and validate a customer's license for the Licenser Module.

### Structure

The license key JWT consists of three parts: Header, Payload, and Signature.

#### Header
```json
{
  "typ": "JWT",
  "alg": "HS256"
}
```

#### Payload
```json
{
  "sub": "License Key",
  "iss": "CAPE Groep",
  "customerid": "<UUID>",
  "licenseid": "<UUID>",
  "customername": "<Customer Name>",
  "iat": <Unix Timestamp>
}
```

- `sub`: Subject of the token (always "License Key")
- `iss`: Issuer of the token
- `customerid`: Unique identifier for the customer
- `licenseid`: Unique identifier for the license
- `customername`: Name of the customer
- `iat`: Issued At timestamp

#### Signature
The signature is created using the HMAC SHA256 algorithm.

### Usage

The license key is used to:
1. Identify the customer and their specific license
2. Validate the authenticity of the license
3. Initiate the license verification process with the server

## Authorization token

The authorization token is a JWT used to authorize and manage access to specific services and applications for a licensed customer.

### Structure

The authorization token JWT also consists of three parts: Header, Payload, and Signature.

#### Header
```json
{
  "alg": "RS256",
  "typ": "JWT"
}
```

#### Payload
```json
{
  "iss": "Cape Groep",
  "sub": "authorization_token",
  "iat": <Unix Timestamp>,
  "exp": <Unix Timestamp>,
  "services": "<JSON string of service objects>",
  "appurls": "<JSON string of application URL objects>",
  "customername": "<Customer Name>",
  "expirationdate": <Unix Timestamp>
}
```

- `iss`: Issuer of the token
- `sub`: Subject of the token (always "authorization_token")
- `iat`: Issued At timestamp
- `exp`: Expiration timestamp
- `services`: JSON string containing an array of service objects
- `appurls`: JSON string containing an array of application URL objects
- `customername`: Name of the customer
- `expirationdate`: License expiration date as a Unix timestamp

#### Examples of Payload Fields

1. `services` example:
```json
{
  "services": [
    {
      "serviceName": "LogTransporter",
      "serviceValue": "LOG_TRANSPORTER"
    },
    {
      "serviceName": "AppMetrics",
      "serviceValue": "APP_METRICS"
    },
    {
      "serviceName": "CAPE Scheduler",
      "serviceValue": "CAPE_SCHEDULER"
    },
    {
      "serviceName": "Embedded Power BI",
      "serviceValue": "POWER_BI_EMBEDDED"
    },
    {
      "serviceName": "Voyager Map",
      "serviceValue": "VOYAGER_MAP"
    }
  ]
}
```
This example shows an array of service objects, each containing a `serviceName` and `serviceValue`. These represent the licensed services available to the customer. The client side can check this to allow / reject using the service

2. `appurls` example:
```json
{
  "appurls": [
    {
      "URL": "http://localhost:8080/"
    },
    {
      "URL": "https://capescheduler-sandbox.mxapps.io/"
    },
    {
      "URL": "http://localhost:8081/"
    },
    {
      "URL": "https://dockulardemo-sandbox.mxapps.io/"
    },
    {
      "URL": "https://embeddedpowerbi-sandbox.mxapps.io/"
    }
  ]
}
```
This example shows an array of application URL objects, each containing a `URL` field. These represent the allowed application URLs for the customer's license. The client side can check this to allow / reject certain app to use the service

3. `customername` example:
```json
"customername": "CAPE Groep B.V."
```
This field simply contains the name of the customer as a string.


#### Signature
The signature is created using the RSA SHA256 algorithm.

### Usage

The authorization token is used to:
1. Authorize access to specific services and applications
2. Manage the duration of the session
3. Provide information about the licensed services and allowed application URLs
4. Validate the customer's access rights during runtime

Both the license key and authorization token play crucial roles in the Licenser Module's security and access control system, ensuring that only authorized customers can use the licensed modules and services.

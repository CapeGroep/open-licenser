# API Documentation
This is the service provided by the server side verificator

## Endpoint: /verify-license

### Method: POST

### Description
Verifies a [license key](data-structure.md#license-key) and generates an [authorization token](data-structure.md#authorization-token).

### Request Body
```json
{
  "licensekey": "string"
}
```

### Response

#### Success (200 OK)
```json
{
  "message": "string"
}
```
The `message` field contains the generated [authorization token](data-structure.md#authorization-token).

#### Error Responses

- 400 Bad Request
  - INVALID_LICENSE: The provided license key is invalid.
  - LICENSE_REVOKED: The license has been revoked.
  - LICENSE_EXPIRED: The license has expired.
  - BAD_LICENSE_SERVER_REQUEST: The request body is malformed.
  - MISSING_LICENSE_KEY: The licensekey field is missing from the request body.

- 500 Internal Server Error
  - INTERNAL_LICENSE_SERVER_ERROR: An unexpected error occurred on the server.

### Authentication
No authentication required. The license key in the request body is used for verification.

### Notes
- The [license key](data-structure.md#license-key) should be a valid JWT token signed with HMAC.
- The [authorization token](data-structure.md#authorization-token) returned is a JWT token signed with RSA.
- The [authorization token](data-structure.md#authorization-token) includes information about licensed services, allowed app URLs, customer name, and expiration date.

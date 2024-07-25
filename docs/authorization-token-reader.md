# Authorization Token Reader

## Purpose

The `AuthorizationTokenReader` class is designed to validate Authorization Tokens in-browser for React-based Mendix pluggable widgets. It ensures that the widget is being used with a valid license by checking various aspects such as the token's signature, expiration date, service ID, and application URL.

## Resources
- `/resources/widget-side/AuthorizationTokenReader.ts`

## Dependencies

The `AuthorizationTokenReader` relies on the following dependency:

- `jose`: A JavaScript module that provides JSON Object Signing and Encryption (JOSE) functionality, including JSON Web Token (JWT) validation.

To install the dependency, run:

```bash
npm install jose
```

## Usage

### 1. Import the necessary functions

```typescript
import { validateLicense, AuthorizationTokenReader } from './path/to/AuthorizationTokenReader';
```

### 2. Customize the AuthorizationTokenReader

Before using the `AuthorizationTokenReader`, you need to customize two important properties:

```typescript
// Set the service ID for your specific widget or module
AuthorizationTokenReader.serviceId = "YOUR_SERVICE_ID";

// Set the public key used to verify the JWT signature
AuthorizationTokenReader.publicKey = `-----BEGIN PUBLIC KEY-----
YOUR_PUBLIC_KEY_HERE
-----END PUBLIC KEY-----`;
```

Ensure that you replace `YOUR_SERVICE_ID` with the unique identifier for your service, and `YOUR_PUBLIC_KEY_HERE` with the actual public key provided by your licensing server.

### 3. Validate the license in your widget

In your widget's React component, use the `validateLicense` function to check if the current license is valid:

```typescript
import { ReactElement, useState, useEffect } from "react";
import { validateLicense } from './path/to/AuthorizationTokenReader';

export function YourWidget(): ReactElement {
    const [isLicenseValid, setIsLicenseValid] = useState<boolean>(false);

    useEffect(() => {
        const checkLicense = async () => {
            // Assume you have a way to get the authorization token
            const authorizationToken = getAuthorizationToken();
            const isValid = await validateLicense(authorizationToken);
            setIsLicenseValid(isValid);
        };

        checkLicense();
    }, []);

    if (!isLicenseValid) {
        return <div>Invalid license. Please contact support.</div>;
    }

    // Rest of your widget code
    return (
        // Your widget JSX
    );
}
```

### 4. Customizing the license validation

For more control over the validation process or to handle specific error cases, use the `AuthorizationTokenReader` class directly:

```typescript
import { AuthorizationTokenReader } from './path/to/AuthorizationTokenReader';

const licenseReader = AuthorizationTokenReader.instance;
const authorizationToken = getAuthorizationToken();

const result = await licenseReader.getResult(authorizationToken);

if (result.valid) {
    // License is valid, proceed with widget rendering
} else {
    console.error(`License validation failed: ${result.reason}`);
    // Handle the specific error case
}
```

## Key Features

1. **JWT Validation**: Uses the `jose` library to verify the JWT signature and decode the payload.
2. **Expiration Check**: Ensures the license has not expired.
3. **Service ID Verification**: Confirms that the token includes the correct service ID for your widget.
4. **App URL Matching**: Validates that the current application URL matches one of the authorized URLs in the token.
5. **Local Development Support**: Allows bypassing license checks for local development environments (configurable).

## Configuration

The `AuthorizationTokenReader` class includes some static properties that you need to configure:

- `AuthorizationTokenReader.publicKey`: The public key used to verify the JWT signature. This should match the public key provided by your licensing server.
- `AuthorizationTokenReader.serviceId`: The service ID for your specific widget or module. This should match the unique identifier for your service.
- `ALLOW_LOCAL_NO_LICENSE`: A boolean flag to bypass license checks in local development environments. Set to `false` in production.

## Error Handling

The `AuthorizationTokenReader` provides detailed error reasons when validation fails. These can be found in the `AuthorizationTokenReader.LicenseInvalidReason` object. You can use these reasons to provide more specific error messages to users or for debugging purposes.

## Security Considerations

1. While the public key doesn't need to be kept secret (it's public by nature), ensure that you're using the correct public key provided by your licensing server. Using an incorrect or outdated public key could lead to improper validation of licenses.
2. Ensure that the `ALLOW_LOCAL_NO_LICENSE` flag is set to `false` in production environments to prevent unauthorized use of your widget.
3. Regularly update the `jose` library to benefit from the latest security patches and improvements in JWT handling.
4. Be cautious about exposing detailed error messages to end-users, as they might reveal information about your licensing mechanism. Consider logging detailed errors server-side and providing generic messages to users.
# License Verificator Documentation

## Overview

The License Verificator is an example Mendix wrapper implementation for `org.capegroep.LicenseManager`. This implementation allows you to integrate license verification into modules that you want to protect.

The License Verificator consists of Domain Models, Microflows, Snippets, and Java Actions, including the `org.capegroep.LicenseManager` itself.

## Resources
- `/resources/open-licenser/OpenLicenser.mpr`

## Dependencies

The module has the following dependency:

- **fastjson2**: This library is already included as a vendorlib in the project.

## Components

### Domain Models

1. **LicenseVerificationResult** (Non-persistent entity)
   - Result (Boolean)
   - Message (String)
   - CustomerName (String)
   - ValidUntil (DateTime)
   - UserMessage (String)

   This entity contains the verification result.

2. **Configuration** (Persistent entity)
   - LicenseKey (String)

   This entity stores your License key information.

### Java Actions

1. **AfterStartup_Java_Action**
   - Initializes `org.capegroep.LicenseManager` with the Configuration entity (License key)
   - Starts the internal check timer

2. **CheckLicenseStatus_Java_Action**
   - Retrieves the LicenseVerificationResult object from the Java part

3. **RetrieveAuthorizationToken_Java_Action**
   - Retrieves the authorization token
   - Useful for in-browser validation, particularly for widgets

4. **UpdateLicense_Java_Action**
   - Updates the current license key cached in `org.capegroep.LicenseManager`

### Microflows

1. **DS_CheckLicenseStatus**
   - Data source for Configuration snippet
   - Uses CheckLicenseStatus_Java_Action
   - Enhances the result with a human-readable message (UserMessage)
   - Returns the object to the page

2. **FetchOrCreate_Configuration**
   - Used in AfterStartup and DS_CheckLicenseStatus
   - If Configuration does not exist, it automatically creates one

3. **Save_Configuration**
   - Stores the license key in the database
   - Updates the cached key using UpdateLicense_Java_Action

### USE_ME Components

1. **Configuration Snippet**
   - Contains necessary user-facing forms to view License information and status
   - Includes input for License key
   - Utilizes LicenseVerificationResult object, Save_Configuration, DS_CheckLicenseStatus, and FetchOrCreate_Configuration

2. **LicenseVerificator_AfterStartup Microflow**
   - Contains the AfterStartup Java part
   - Used in Mendix AfterStartup

3. **LicenseVerificatorConfiguration Page**
   - A page containing the Configuration snippet
   - Allows users to navigate to it from the home screen

## Integration

To protect your own module, you need to use the result from `CheckLicenseStatus_Java_Action` in combination with `RetrieveAuthorizationToken_Java_Action`.

## Important Notes

1. You must update the Verification server URL inside `org.capegroep.LicenseManager`.
2. You must update the public key inside `org.capegroep.LicenseManager`.
3. You must update the service name to the name of the service you want to protect.
4. This implementation is not obfuscated for clarity. Consider putting the `org.capegroep.LicenseManager` in a JAR file for production use.
5. The fastjson2 library is included as a vendorlib, so no additional setup is required for this dependency.

## Best Practices

1. Regularly update the license key and verification mechanisms to maintain security.
2. Implement proper error handling and user feedback for license verification failures.
3. Consider implementing a grace period for license expiration to allow for renewal without immediate service interruption.
4. Regularly test the license verification process to ensure it's working as expected.
5. Implement logging for license verification attempts and results for troubleshooting and auditing purposes.

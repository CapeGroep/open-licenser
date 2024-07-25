# Open Licenser Solution

## Target User

This module is designed for Mendix Module/Widget solution creators who want to have control over the licensing terms of their creations.

## General Business Requirements

1. Control who can use the licensed module
2. Control which applications can use the licensed module
3. Control for how long the licensed module can be used
4. License verification
5. Obfuscation (Not tamper-resistant)
6. Easy to manage for module creators
7. Easy to set up for clients

## Why We Did It

We created this Open Licenser to provide Mendix developers with a robust solution for managing and monetizing their custom modules and widgets. This system allows for flexible licensing models, including subscription-based monetization, while maintaining control over distribution and usage.

## General Overview of the Solution

The Open Licenser is a server-based license checking system that consists of several components:

1. A server-side infrastructure using AWS Lambda and DynamoDB for high availability
2. A client-side integration for license verification (Java package and Typescript class)
3. A License Manager application for easy license management (Called the Open Licenser)

The system allows for real-time license revocation and easy management of license keys, prioritizing these features over simplicity.


## Included resources

```
resources/
├── java-module/
│   └── org/
│       └── capegroep/
│           └── licensemanager/
│               ├── CAPELicenseVerifier.java
│               ├── License.java
│               ├── LicenseManager.java
│               └── Result.java
├── lambda/
│   └── app.py
├── open-licenser/
│   └── OpenLicenser.mpk
└── widget-side/
    └── AuthorizationTokenReader.ts
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

For more information, please contact:

- info@capegroep.nl

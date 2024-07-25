import * as jose from "jose";

const ALLOW_LOCAL_NO_LICENSE = true;

export const validateLicense = async (licenseToken: string): Promise<boolean> => {
    const AuthorizationTokenReader = AuthorizationTokenReader.instance;
    const result = await AuthorizationTokenReader.getResult(licenseToken);
    if (!result.valid) {
        console.error(result.reason);
    }
    return result.valid;
};

export class AuthorizationTokenReader {
    static instance = new AuthorizationTokenReader();
    static publicKey = `-----BEGIN PUBLIC KEY-----
    -----END PUBLIC KEY-----`;
    static serviceId = "<YOUR SERVICE>";
    static LicenseInvalidReason = {
        INVALIDJWT: "Invalid JWT",
        INVALIDPAYLOAD: "Invalid PayLoad",
        INVALIDAPPURL: "Invalid app url",
        INVALIDPRODUCT: "Invalid product",
        INVALIDSIGNATURE: "Invalid license key",
        INVALIDSERVICEID: "Invalid service",
        EXPIRED: "License is expired"
    };

    getDomainName = (domain: string): string => {
        const parts = domain.split(".").reverse();
        const cnt = parts.length;
        if (cnt >= 3) {
            // see if the second level domain is a common SLD.
            if (parts[1].match(/^(com|edu|gov|net|mil|org|nom|co|name|info|biz|io|nl)$/i)) {
                return parts[2] + "." + parts[1] + "." + parts[0];
            }
        }

        return parts[1] + "." + parts[0].toLocaleLowerCase();
    };

    urlIsMxApps = () => {
        const regexMxApps = new RegExp("^https://.+\\.mxapps.io/?$", "g");
        // @ts-ignore
        return regexMxApps.test(location.href);
    };

    urlIsLocal = (): boolean => {
        return (
            location.hostname === "localhost" ||
            location.hostname === "127.0.0.1" ||
            location.hostname.startsWith("192.") ||
            location.hostname.startsWith("10.")
        );
    };

    matchAppUrls = (appUrls: { URL: string }[]): boolean => {
        const mendixAppUrlHostName = location.origin;
        return appUrls.some(url => mendixAppUrlHostName === url.URL.toLowerCase().replace(/\/$/, ""));
    };

    verify = async (JWT: string): Promise<boolean | any> => {
        try {
            const ecPublicKey = await jose.importSPKI(AuthorizationTokenReader.publicKey, "RS256");
            const { payload } = await jose.jwtVerify(JWT, ecPublicKey);

            if (payload.sub && payload.sub === "authorization_token") {
                return payload;
            } else {
                return false;
            }
        } catch (err) {
            return false;
        }
    };

    getResult = async (token: string): Promise<{ valid: boolean; reason?: string }> => {
        if (ALLOW_LOCAL_NO_LICENSE && this.urlIsLocal()) {
            return {
                valid: true
            };
        }
        const verifiedPayloadSub = await this.verify(token);

        if (!verifiedPayloadSub) {
            return {
                valid: false,
                reason: AuthorizationTokenReader.LicenseInvalidReason.INVALIDSIGNATURE
            };
        }

        let payloadObject;
        try {
            payloadObject = verifiedPayloadSub;
        } catch (err) {
            return {
                valid: false,
                reason: AuthorizationTokenReader.LicenseInvalidReason.INVALIDPAYLOAD
            };
        }

        if (new Date(payloadObject.expirationDate).getTime() < new Date().getTime()) {
            return {
                valid: false,
                reason: AuthorizationTokenReader.LicenseInvalidReason.EXPIRED
            };
        }

        const services: {
            serviceName: string;
            serviceValue: string;
        }[] = JSON.parse(payloadObject.services);

        if (services === undefined || !services.find(service => service.serviceValue == AuthorizationTokenReader.serviceId)) {
            return {
                valid: false,
                reason: AuthorizationTokenReader.LicenseInvalidReason.INVALIDSERVICEID
            };
        }

        const appUrls: { URL: string }[] = JSON.parse(payloadObject.appurls);
        if (!this.matchAppUrls(appUrls)) {
            return {
                valid: false,
                reason: AuthorizationTokenReader.LicenseInvalidReason.INVALIDAPPURL
            };
        }

        return {
            valid: true
        };
    };
}

package org.capegroep.licensemanager;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CAPELicenseVerifier {
    private static final String SERVICE_ID = "<YOUR_SERVICE>";
    private static final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n" +
            "-----END PUBLIC KEY-----";

    private CAPELicenseVerifier() {
    }

    public static License decodeTokenToLicense(String authorizationToken) throws NoSuchAlgorithmException, InvalidKeySpecException {
        DecodedJWT authToken = decodeLicense(authorizationToken);
        return new License(authToken);
    }

    public static Result verifyLicense(URL appURL, License license) throws MalformedURLException {
        if (!license.getServices().contains(SERVICE_ID)) return new Result(false, "INVALID_SERVICE_ID");
        if ((license.getLicenseExpirationDate().getTime() - new Date().getTime()) < 0)
            return new Result(false, "LICENSE_EXPIRED");
        if (!validateUrls(appURL, license.getAppUrls())) return new Result(false, "INVALID_APPURL");
        if (isLocalUrl(appURL.getHost())) return new Result(true, "LOCAL_URL");
        return new Result(true, "");
    }

    private static boolean validateUrls(URL url, ArrayList<String> appUrls) throws MalformedURLException {
        boolean isValid = false;
        for (String domain : appUrls) {
            if (domain.startsWith("re:")) {
                String regexPattern = domain.substring(3);
                Pattern pattern = Pattern.compile(regexPattern);
                Matcher matcher = pattern.matcher(url.getHost());
                isValid = matcher.find();
                if(isValid) break;
            } else {
                String host = new URL(domain).getHost();
                isValid = host.equalsIgnoreCase(url.getHost());
                if(isValid)break;
            }
        }
        return isValid;
    }

    private static DecodedJWT decodeLicense(String license) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Algorithm algorithm = Algorithm.RSA256(getPublicKey(), null);
        JWT.require(algorithm)
                .build()
                .verify(license);
        return JWT.decode(license);
    }

    private static RSAPublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] decodedKey = Base64.getDecoder().decode(PUBLIC_KEY.replaceAll("-----(BEGIN|END) PUBLIC KEY-----", "").replaceAll("\n", ""));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    private static boolean isLocalUrl(String url) {
        return url.contentEquals("localhost") ||
                url.contentEquals("127.0.0.1") ||
                url.startsWith("192.") ||
                url.startsWith("10.");
    }
}

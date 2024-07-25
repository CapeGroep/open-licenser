package org.capegroep.licensemanager;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.mendix.core.Core;
import com.mendix.logging.ILogNode;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LicenseManager {
    private static final ILogNode logger = Core.getLogger(LicenseManager.class.getName());
    private static final String VERIFICATION_SERVER_URL = "<YOUR_VERIFICATION_URL>";

    private static LicenseManager instance = null;
    private URL appURL;
    private String licenseKey;
    private String authorizationToken;
    private License license;
    private Result validationResult = new Result(false, "NOT_INITIALIZED");
    private static boolean isLicenseValidationAnnounced = false;
    private ScheduledExecutorService scheduler;
    private static ScheduledFuture<?> taskHandle;

    private LicenseManager() {

    }

    @SuppressWarnings("unused")
    public static LicenseManager getInstance() {
        if (instance == null) {
            instance = new LicenseManager();
        }
        return instance;
    }

    @SuppressWarnings("unused")
    public void setLicenseKey(URL appUrl, String licenseKey) {
        this.appURL = appUrl;
        this.licenseKey = licenseKey;
        isLicenseValidationAnnounced = false;
        startVerificationService();
    }

    @SuppressWarnings("unused")
    public License getLicense() {
        return license;
    }

    @SuppressWarnings("unused")
    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public Result getValidationResult() {
        return validationResult;
    }

    private void startVerificationService() {
        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
        }

        if (taskHandle != null) {
            taskHandle.cancel(true);
        }

        // Always do the first token request immediately
        requestAndVerifyAuthorizationToken();

        // Start the verification service
        Runnable dailyAuthorizationTokenVerificationTask = this::requestAndVerifyAuthorizationToken;
        taskHandle = scheduler.scheduleAtFixedRate(dailyAuthorizationTokenVerificationTask,
                1, 1, TimeUnit.DAYS);
    }

    private void announceValidationResult(String message, Throwable e) {
        if (isLicenseValidationAnnounced) return;
        logger.info(message, e);
        isLicenseValidationAnnounced = true;
    }

    private void requestAndVerifyAuthorizationToken() {
        boolean isAppUrlEmpty = this.appURL == null || this.appURL.toString().isEmpty();
        boolean isLicenseKeyEmpty = this.licenseKey == null || this.licenseKey.isEmpty();

        if (isAppUrlEmpty || isLicenseKeyEmpty) {
            this.validationResult = new Result(false, "LICENSE_NOT_FOUND");
            announceValidationResult(this.validationResult.getMessage(), null);
            return;
        }

        Result authorizationResult = requestAuthorizationToken(licenseKey);
        try {
            if (authorizationResult.getResult()) {
                handleValidAuthorizationToken(authorizationResult);
            } else {
                handleInvalidAuthorizationToken(authorizationResult);
            }

        } catch (MalformedURLException | NoSuchAlgorithmException | InvalidKeySpecException | RuntimeException e) {
            logger.error("error verifying license", e);
            announceValidationResult("Error Verifying licence", e);
            this.authorizationToken = null;
            this.license = null;
            this.validationResult = new Result(false, "LOGTRANSPORTER_INTERNAL_ERROR");
        }
    }

    private void handleValidAuthorizationToken(Result authorizationResult) throws NoSuchAlgorithmException, InvalidKeySpecException, MalformedURLException, RuntimeException {
        String token = authorizationResult.getMessage();
        License license = CAPELicenseVerifier.decodeTokenToLicense(token);
        processLicense(license);
        if (validationResult.getResult()) {
            this.authorizationToken = token;
        }
    }

    private void handleInvalidAuthorizationToken(Result authorizationResult) throws NoSuchAlgorithmException, InvalidKeySpecException, RuntimeException, MalformedURLException {
        // We can try using the old license if the issue is connection
        if (this.authorizationToken != null && authorizationResult.getMessage().contentEquals("CONNECTION_WITH_LICENSE_SERVER_FAILED")) {
            License license = CAPELicenseVerifier.decodeTokenToLicense(this.authorizationToken);
            processLicense(license);
        } else {
            // Otherwise we should clear the license
            // And return why authorization failed as the validation result.
            this.license = null;
            this.validationResult = authorizationResult;
            announceValidationResult(authorizationResult.getMessage(), null);
        }
    }

    private void processLicense(License license) throws MalformedURLException, RuntimeException {
        this.validationResult = CAPELicenseVerifier.verifyLicense(appURL, license);
        if (validationResult.getResult()) {
            this.license = license;
            announceValidationResult("Licensed to " + license.getCustomerName() + ". Valid until: " + license.getLicenseExpirationDate(), null);
        } else {
            this.license = null;
        }
    }

    private static Result requestAuthorizationToken(String license) {
        HttpClient client = HttpClient.newHttpClient();

        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(buildLicenseVerificationRequest(license)))
                    .uri(URI.create(VERIFICATION_SERVER_URL))
                    .header("Content-Type", "application/json") // Set content type header
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    String jwt = parseHTTPSuccessResponse(response.body());
                    return new Result(true, jwt);
                } else if (response.statusCode() == 400) {
                    String error = parseHTTPErrorResponse(response.body());
                    return new Result(false, error);
                }
            } catch (IOException | InterruptedException | JSONException e) {
                logger.debug("Exception when requesting authorization token", e);
            }

            // Delay before retrying
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // Handle interruption gracefully if needed
                logger.debug("Sleep interrupted", e);
                break;
            }
        }

        // Handle case where all retries fail
        return new Result(false, "CONNECTION_WITH_LICENSE_SERVER_FAILED");
    }

    private static String buildLicenseVerificationRequest(String license) {
        JSONObject request = new JSONObject();
        request.put("licensekey", license);
        return request.toJSONString();
    }

    private static String parseHTTPSuccessResponse(String payload) throws JSONException {
        JSONObject json = JSON.parseObject(payload);
        return json.getString("message");
    }

    private static String parseHTTPErrorResponse(String payload) {
        JSONObject json = JSON.parseObject(payload);
        return json.getString("error");
    }
}



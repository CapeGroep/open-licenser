# AWS Infrastructure Setup

This document provides step-by-step instructions for setting up the server-side components of the Open Licenser on AWS. Follow these steps to configure DynamoDB, generate an RSA key using KMS, add a secret to AWS Secrets Manager, and set up a Lambda function for the `app.py` script.

## Resources
- `/resources/lambda/app.py`

## Setting up DynamoDB

1. Open the AWS Management Console and navigate to the DynamoDB service.
2. Click on "Create table".
3. Enter a table name (e.g., "LicenserDB").
4. For the partition key, enter "licenseid" and select "String" as the data type.
5. Leave the sort key empty.
6. Under "Table settings", choose "Customize settings".
7. Select "DynamoDB Standard" for the table class.
8. Choose "Provisioned" capacity mode and set read/write capacity units as needed.
9. Enable "Auto scaling" for read and write capacity.
10. Under "Encryption at rest", choose "AWS owned key" or "AWS managed key" based on your security requirements.
11. Click "Create table".
12. Implement the required DynamoDB Schema

After creation, note down the ARN of the table for use in the Lambda function's IAM role.

## Generating RSA Key through KMS

1. Go to the AWS Key Management Service (KMS) in the AWS Management Console.
2. Click on "Create key".
3. Choose "Asymmetric" for the key type.
4. Select "Sign and verify" for the key usage.
5. Choose "RSA_2048" for the key spec.
6. Click "Next".
7. Add an alias for the key (e.g., "LicenserRSAKey").
8. Add any necessary tags and click "Next".
9. Define key administrators and click "Next".
10. Define key usage permissions and click "Next".
11. Review the key policy and click "Finish".

After creation, note down the Key ID for use in the Lambda function.

## Adding Random Secret to AWS Secrets Manager

1. Navigate to AWS Secrets Manager in the AWS Management Console.
2. Click on "Store a new secret".
3. Choose "Other type of secret".
4. Add a key-value pair:
   - Key: `key`
   - Value: Generate a random string (e.g., using `openssl rand -base64 32`)
5. Click "Next".
6. Enter a secret name (e.g., "LicenserJWTSecret").
7. Add a description if needed.
8. Click "Next".
9. Configure rotation settings if required (optional).
10. Click "Next".
11. Review the details and click "Store".

Note down the Secret ARN for use in the Lambda function.

## Setting up Lambda for app.py

1. Go to the AWS Lambda service in the AWS Management Console.
2. Click on "Create function".
3. Choose "Author from scratch".
4. Enter a function name (e.g., "LicenserFunction").
5. Select "Python 3.10" (or the latest available Python version) as the runtime.
6. Under "Permissions", choose "Create a new role with basic Lambda permissions".
7. Click "Create function".
8. In the "Code" tab, replace the default code with the contents of your `app.py` file.
9. In the "Configuration" tab:
   - Set the handler to "app.lambda_handler".
   - Adjust the timeout to an appropriate value (e.g., 30 seconds).
   - Add environment variables:
     - `LICENSER_DB`: The name of your DynamoDB table
     - `JWT_SECRETS_NAME`: The name of your JWT secret in Secrets Manager
     - `KMS_KEY_ID`: The Key ID of your RSA key in KMS
10. In the "Permissions" tab:
    - Open the execution role.
    - Add the following policies:
      - AmazonDynamoDBFullAccess (or a more restrictive custom policy)
      - AWSKeyManagementServicePowerUser (or a more restrictive custom policy)
      - SecretsManagerReadWrite (or a more restrictive custom policy)
11. Deploy the function by clicking "Deploy" in the "Code" tab.

To test the function:
1. Create a test event with the expected [input](api-documentation.md#endpoint-verify-license)
2. Run the test and verify the output.

Remember to set up API Gateway if you want to expose this Lambda function as an HTTP endpoint, or you can use Lambda function URL for testing.

---

This setup provides a basic configuration for the Licenser Module's server-side components. Ensure to follow AWS best practices for security, including using the principle of least privilege for IAM roles and regularly rotating secrets and keys.

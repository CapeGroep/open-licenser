# Setting Up the Open Licenser

The Open Licenser is a Mendix-based application that allows you to manage licenses for your Mendix modules and widgets. Open Licenser will sync the licenses into DynamoDB.


## Resources
`/resources/open-licenser/OpenLicenser.mpr`

## Setup Steps

1. **Extract the MPR File**
   - Locate the Open Licenser MPR file in your project directory
   - Extract the contents of the MPR file

2. **Deploy to the Cloud**
   - Open Mendix Studio Pro
   - Load the extracted Open Licenser project
   - Follow the standard Mendix deployment process to deploy the application to your preferred cloud environment

3. **Modify Constants**
   After deploying the application, you need to modify several constants to ensure proper functionality. Update the following constants with your specific values:

   - `AWSAuthentication.AccessKey`: Your AWS Access Key
   - `AWSAuthentication.SecretAccessKey`: Your AWS Secret Access Key
   - `LicenseSyncronizer.DynamoDBIndexName`: The name of your DynamoDB index for storing license information
   - `LicenseSyncronizer.DynamoDBTableName`: The name of your DynamoDB table for storing license information
   - `MendixLicenser.JWTSecrets`: The secret key used for JWT token signing. Should be the same as what you store inside AWS Secret Manager

4. **Verify Setup**
   - After updating the constants, restart your application
   - Attempt to create a new license to ensure the connection to AWS services is working correctly

## Security Considerations

- Ensure that your AWS credentials have the minimum necessary permissions to interact with DynamoDB and other required AWS services
- Regularly rotate your AWS access keys and update the constants accordingly
- Keep your JWT secret secure and consider implementing a secret rotation strategy

## Troubleshooting

If you encounter issues during setup:
- Double-check that all constants are correctly set
- Verify that your AWS credentials are valid and have the necessary permissions
- Check the application logs for any error messages
- Ensure that the specified DynamoDB table exists and is accessible
- Licence manager assume to use eu-central-1. If your server side is in another AWS Region, please change putItem activity in SUB_PutLicenseToAWS

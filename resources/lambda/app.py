import os
import boto3
import json
from botocore.exceptions import ClientError
from dateutil import parser
from datetime import datetime, timezone, timedelta
import jwt
import base64

dynamodb = boto3.resource('dynamodb')

def lambda_handler(event, _context):
    try:
        license_key = get_license_key_from_request(event)
        licence_data = get_license_data(license_key)
        if 'Item' not in licence_data:
            return error_response(400, 'INVALID_LICENSE')
        license_info = licence_data["Item"]
        if license_info.get('isrevoked', False):
            return error_response(400, 'LICENSE_REVOKED')
        
        expiration_date = parser.parse(license_info["expirationdate"])
        if datetime.now(timezone.utc) > expiration_date:
            return error_response(400, 'LICENSE_EXPIRED')

        authorization_token = generate_authorization_token(
                license_info['services'],
                license_info['appurls'],
                license_info['customername'],
                expiration_date
            )
        return success_response(authorization_token)
    
    except json.JSONDecodeError:
        return error_response(400, 'BAD_LICENSE_SERVER_REQUEST')
    except ValueError as e:
        return error_response(400, e)
    except jwt.DecodeError:
        return error_response(400, 'INVALID_LICENSE')
    except ClientError as e:
        print (e)
        return error_response(500, 'INTERNAL_LICENSE_SERVER_ERROR')

def get_license_key_from_request(event):
    body = json.loads(event['body'])
    if 'licensekey' not in body:
        raise ValueError('MISSING_LICENSE_KEY')
    return body['licensekey']

def get_license_data(license_key):
    secret = get_secret(os.environ['JWT_SECRETS_NAME'])
    decoded_jwt = jwt.decode(license_key, secret, algorithms=["HS256"])
    license_id = decoded_jwt["licenseid"]

    license_table = dynamodb.Table(os.environ['LICENSER_DB'])
    response = license_table.get_item(Key={'licenseid': license_id})
    # TODO: License not found
    return response

def generate_authorization_token(services, appurls, customerName, expirationdate):
    header = {
        "alg": "RS256",
        "typ": "JWT"
    }

    payload = {
        'iss': 'YOUR ISSUER',
        'sub': 'authorization_token',
        'iat': int(datetime.utcnow().timestamp()),
        'exp': int((datetime.utcnow() + timedelta(days=90)).timestamp()), # expired every 30 days
        'services': services,
        'appurls': appurls,
        'customername': customerName,
        'expirationdate': expirationdate.timestamp()
    }

    kms_key_id = os.environ['KMS_KEY_ID']
    return sign_jwt_with_kms(header,payload, kms_key_id)

def sign_jwt_with_kms(header, payload, kms_key_id):
    encoded_header = base64.urlsafe_b64encode(json.dumps(header).encode()).rstrip(b'=')
    encoded_payload = base64.urlsafe_b64encode(json.dumps(payload).encode()).rstrip(b'=')
    message = encoded_header + b'.' + encoded_payload

    kms_client = boto3.client('kms')
    response = kms_client.sign(
        KeyId=kms_key_id,
        Message=message,
        MessageType='RAW', 
        SigningAlgorithm='RSASSA_PKCS1_V1_5_SHA_256' 
    )
    # Extract the signature
    signature = base64.urlsafe_b64encode(response['Signature']).rstrip(b'=')

    # Construct the JWT Token    
    jwt_token = (encoded_header + b'.' + encoded_payload + b'.' + signature).decode()

    return jwt_token

def get_secret(secret_name):
    """Retrieves the secret from AWS Secrets Manager"""
    secrets_manager_client = boto3.client('secretsmanager')
    response = secrets_manager_client.get_secret_value(SecretId=secret_name)
    return json.loads(response['SecretString'])['key']


def error_response(status_code, message):
    """Helper function to create error responses"""
    return {
        'statusCode': status_code,
        'body': json.dumps({'error': message})
    }

def success_response(message):
    """Helper function to create success responses"""
    return {
        'statusCode': 200,
        'body': json.dumps({'message': message})  # More flexible 
    }

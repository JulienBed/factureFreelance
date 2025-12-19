#!/bin/bash

# Generate RSA key pair for JWT signing
echo "Generating RSA key pair for JWT..."

mkdir -p src/main/resources/META-INF

# Generate private key
openssl genrsa -out src/main/resources/META-INF/privateKey.pem 2048

# Generate public key from private key
openssl rsa -in src/main/resources/META-INF/privateKey.pem -pubout -out src/main/resources/META-INF/publicKey.pem

echo "JWT keys generated successfully!"
echo "Private key: src/main/resources/META-INF/privateKey.pem"
echo "Public key: src/main/resources/META-INF/publicKey.pem"

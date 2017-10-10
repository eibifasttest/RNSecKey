# README #

This README would normally document whatever steps are necessary to get your application up and running.

### What is this repository for? ###

This is for fingerprint

### How do I get set up? ###

add this in package.json

then run react-native link

### What are the method? ###

callback are in (err, result) format, check for err, if no err, get from result

#### generateKey: (callback)  ####
method to generate a key pair

#### getPublicKey: (callback) ####
method to get public key from generated key pair

#### getSignature: (nonce, callback) ####
method to sign the nonce with generated private key

#### removeKeyPair: (callback) ####
method to remove generated key pair

####isFingerprintSupported: (callback) ####
method to check if device support fingerprint

#### isLockScreenEnabled: (callback) ####
method to check if device with secure lock screen (eg. passcode, pin, pattern, etc)

#### isEligibleForFingerprint: (callback) ####
method to check if device is eligible for subscribe fingerprint feature (a combined version of above 2 api)

#### getDeviceName: (callback) ####
method to get current device name

#### getDeviceVersion: (callback) ####
method to get current device OS version

### Who do I talk to? ###

Tan Boon Kiat / Tang Woon Chee

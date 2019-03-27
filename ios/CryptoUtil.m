//
//  CryptoUtil.m
//  RNSecKey
//
//  Created by Tan Boon Kiat on 9/28/17.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import "CryptoUtil.h"
#import "DeviceUtil.h"
#import "SecKeyConstant.h"

@implementation CryptoUtil

+ (NSObject *)generateKey{
  CFErrorRef error = NULL;
  SecAccessControlRef sacObject;
  
  [self removePrivateKey];
  
  if([DeviceUtil isFingerprintSupported]){
    sacObject = SecAccessControlCreateWithFlags(kCFAllocatorDefault,
                                                kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly,
                                                kSecAccessControlTouchIDCurrentSet | kSecAccessControlPrivateKeyUsage, &error);
  } else {
    sacObject = SecAccessControlCreateWithFlags(kCFAllocatorDefault,
                                                kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly,
                                                kSecAccessControlPrivateKeyUsage, &error);
  }
  
  NSDictionary *parameters;
  NSDictionary *signParameters;
  
  NSError *gen_error = nil;
  
  if([DeviceUtil supportSecureEnclave]){
    parameters = @{
                   (id)kSecAttrTokenID: (id)kSecAttrTokenIDSecureEnclave,
                   (id)kSecAttrKeyType: (id)kSecAttrKeyTypeECSECPrimeRandom,
                   (id)kSecAttrKeySizeInBits: @256,
                   (id)kSecAttrLabel: PRIVATE_KEY_TAG,
                   (id)kSecPrivateKeyAttrs: @{
                       (id)kSecAttrAccessControl: (__bridge_transfer id)sacObject,
                       (id)kSecAttrIsPermanent: @YES,
                       }
                   };
    
    signParameters = @{
                   (id)kSecAttrTokenID: (id)kSecAttrTokenIDSecureEnclave,
                   (id)kSecAttrKeyType: (id)kSecAttrKeyTypeECSECPrimeRandom,
                   (id)kSecAttrKeySizeInBits: @256,
                   (id)kSecAttrLabel: SIGN_PRIVATE_KEY_TAG,
                   (id)kSecPrivateKeyAttrs: @{
                       (id)kSecAttrAccessControl: (__bridge_transfer id)sacObject,
                       (id)kSecAttrIsPermanent: @YES,
                       }
                   };
    
  } else {
    parameters = @{
                   (id)kSecAttrKeyType: (id)kSecAttrKeyTypeECSECPrimeRandom,
                   (id)kSecAttrKeySizeInBits: @256,
                   (id)kSecAttrLabel: PRIVATE_KEY_TAG,
                   (id)kSecPrivateKeyAttrs: @{
                       (id)kSecAttrIsPermanent: @YES,
                       }
                   };
    
    signParameters = @{
                   (id)kSecAttrKeyType: (id)kSecAttrKeyTypeECSECPrimeRandom,
                   (id)kSecAttrKeySizeInBits: @256,
                   (id)kSecAttrLabel: SIGN_PRIVATE_KEY_TAG,
                   (id)kSecPrivateKeyAttrs: @{
                       (id)kSecAttrIsPermanent: @YES,
                       }
                   };
  }
  
  CFBridgingRelease(SecKeyCreateRandomKey((__bridge CFDictionaryRef)parameters, (void *)&gen_error));
  
  return gen_error;
}

+ (SecKeyRef)getPrivateKey:(NSString *)keyTag :(NSString *)message{
  
  NSDictionary *params = @{
                           (id)kSecClass: (id)kSecClassKey,
                           (id)kSecAttrKeyType: (id)kSecAttrKeyTypeECSECPrimeRandom,
                           (id)kSecAttrKeySizeInBits: @256,
                           (id)kSecAttrLabel: keyTag,
                           (id)kSecReturnRef: @YES,
                           (id)kSecUseOperationPrompt: message
                           };
  SecKeyRef privateKey;
  OSStatus status = SecItemCopyMatching((__bridge CFDictionaryRef)params, (CFTypeRef *)&privateKey);
  
  if (status == errSecSuccess) {
    return privateKey;
  }
  return nil;
}

+ (OSStatus)removePrivateKey:(NSString *)keyTag {
  
  NSDictionary *params = @{
                           (id)kSecClass: (id)kSecClassKey,
                           (id)kSecAttrKeyType: (id)kSecAttrKeyTypeECSECPrimeRandom,
                           (id)kSecAttrKeySizeInBits: @256,
                           (id)kSecAttrLabel: keyTag,
                           (id)kSecReturnRef: @YES
                           };
  SecKeyRef privateKey;
  OSStatus status = SecItemCopyMatching((__bridge CFDictionaryRef)params, (CFTypeRef *)&privateKey);
  
  if (status == errSecSuccess) {
    status = SecItemDelete((__bridge CFDictionaryRef)params);
    NSLog(@"remove private key status is %d", (int)status);
    return status;
  }
  return -1;
}

+ (void)removePrivateKey {
  [self removePrivateKey: PRIVATE_KEY_TAG];
  [self removePrivateKey: SIGN_PRIVATE_KEY_TAG];
}

+ (NSData *) getPublicKeyBitsFromKey:(SecKeyRef)publicKey{
  static const uint8_t publicKeyIdentifier[] = "com.RNPlugin.RNSecKey.cryptopublic";
  NSData *publicTag = [[NSData alloc] initWithBytes:publicKeyIdentifier length:sizeof(publicKeyIdentifier)];
  
  OSStatus sanityCheck = noErr;
  NSData * publicKeyBits = nil;
  
  NSMutableDictionary * queryPublicKey = [[NSMutableDictionary alloc] init];
  [queryPublicKey setObject:(__bridge id)kSecClassKey forKey:(__bridge id)kSecClass];
  [queryPublicKey setObject:publicTag forKey:(__bridge id)kSecAttrApplicationTag];
  [queryPublicKey setObject:(__bridge id)kSecAttrKeyTypeECSECPrimeRandom forKey:(__bridge id)kSecAttrKeyType];
  
  // Temporarily add key to the Keychain, return as data:
  NSMutableDictionary * attributes = [queryPublicKey mutableCopy];
  [attributes setObject:(__bridge id)publicKey forKey:(__bridge id)kSecValueRef];
  [attributes setObject:@YES forKey:(__bridge id)kSecReturnData];
  CFTypeRef result;
  sanityCheck = SecItemAdd((__bridge CFDictionaryRef) attributes, &result);
  if (sanityCheck == errSecSuccess) {
    publicKeyBits = CFBridgingRelease(result);
    
    // Remove from Keychain again:
    (void)SecItemDelete((__bridge CFDictionaryRef) queryPublicKey);
  }
  
  return publicKeyBits;
}

+ (NSString *)transformKey:(NSData *)publicKeyData{
  static const unsigned char ecPublicKeyHeader[26] = {
    
    /* Sequence of length 0xd made up of OID followed by NULL */
    0x30, 0x59, 0x30, 0x13, 0x06, 0x07, 0x2A, 0x86, 0x48, 0xCE, 0x3D, 0x02, 0x01,
    0x06, 0x08, 0x2A, 0x86, 0x48, 0xCE, 0x3D, 0x03, 0x01, 0x07, 0x03, 0x42, 0x00
  };
  
  NSMutableData *data = [[NSMutableData alloc] initWithBytes:ecPublicKeyHeader length:26];
  
  [data appendData:publicKeyData];
  
  NSString *keyInBase64 = [data base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength];
  
  return keyInBase64;
}

+ (NSString *)getPublicKey:(NSString *)keyTag {
  SecKeyRef privateKey = [self getPrivateKey:keyTag:@""];
  if (privateKey == nil) {
    return @"";
  }
  id publicKey = (CFBridgingRelease(SecKeyCopyPublicKey(privateKey)));
  NSData *publicKeyData = [self getPublicKeyBitsFromKey:(__bridge SecKeyRef)publicKey];
  return [self transformKey:publicKeyData];
}

+ (NSDictionary *)getPublicKeyMap{
  NSString* publicKey = [self getPublicKey:PRIVATE_KEY_TAG];
  NSString* signPublicKey = [self getPublicKey:SIGN_PRIVATE_KEY_TAG];
  return @{
           @"key": publicKey,
           @"signKey": signPublicKey,
           };
}

+ (NSString *)getSignature:(NSString *)type :(NSString *)nonce message:(NSString *)message error:(NSError **)nsError{
  NSData *toBeSignedData = [nonce dataUsingEncoding:NSUTF8StringEncoding];
  
  NSString* keyTag = @"";
  if ([type isEqualToString:SIGNING]) {
    keyTag = SIGN_PRIVATE_KEY_TAG;
  } else if ([type isEqualToString:AUTHENTICATE]) {
    keyTag = PRIVATE_KEY_TAG;
  }
  
  SecKeyRef privateKey = [self getPrivateKey:keyTag:message];
  NSString *signedNounce = @"";
  if(privateKey != nil){
    NSError *error;
    NSData *signature = CFBridgingRelease(SecKeyCreateSignature(privateKey, kSecKeyAlgorithmECDSASignatureMessageX962SHA256, (CFDataRef)toBeSignedData, (void *)&error));
    signedNounce = [signature base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength];
    NSLog(@"signed data: %@", signedNounce);
    
    NSLog(@"error is what?? %ld", (long)[error code]);
    
    /*
     -1 = failed 3 times
     -2 = cancel
     -3 = biometric state different (add new / delete fingerprint)
     -4 = close screen
     -8 = biometric locked (failed 5 times)
     -1000 = UI activation timed out
     */
    
    if(error != nil){
      *nsError = error;
      NSLog(@"getSignedNonce error code is %ld and whole error is %@", (long)[error code], error);
      if([error code] == -3){
        signedNounce = BIOMETRIC_DIFFERENT_STATE;
      } else if([error code] == -2 || [error code] == -4 || [error code] == -1000){
        signedNounce = BIOMETRIC_CANCEL;
      } else if([error code] == -8){
        signedNounce = BIOMETRIC_LOCKOUT;
      } else if([error code] == -1){
        signedNounce = BIOMETRIC_FAILED_CONSECUTIVE;
      }
    }
  }
  return signedNounce;
}

@end

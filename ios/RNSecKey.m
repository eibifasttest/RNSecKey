#import "RNSecKey.h"
#import "DeviceUtil.h"
#import "CryptoUtil.h"

@implementation RNSecKey

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(generateKey:(RCTResponseSenderBlock)callback){
  NSError *error = (NSError *)[CryptoUtil generateKey];
  
  if(error != nil){
    callback(@[[NSNumber numberWithInteger:[error code]], [error localizedDescription]]);
  }
  callback(@[[NSNull null], [NSNull null]]);
}

RCT_EXPORT_METHOD(getPublicKey:(RCTResponseSenderBlock)callback){
  NSString *publicKeyString = [CryptoUtil getPublicKeyString];
  callback(@[[NSNull null], publicKeyString]);
}

RCT_EXPORT_METHOD(getSignature:(NSString *)nonce callback:(RCTResponseSenderBlock)callback){
  NSError *error;
  NSString *signature = [CryptoUtil getSignature:nonce error:&error];
  
  if(error != nil){
    callback(@[[NSNumber numberWithInteger:[error code]], signature]);
    return;
  }
  callback(@[[NSNull null], signature]);
}

RCT_EXPORT_METHOD(removeKeyPair:(RCTResponseSenderBlock)callback){
  OSStatus status = [CryptoUtil removePrivateKey];
  
  if(status != 0){
    callback(@[[NSNumber numberWithInt:status], [NSNull null]]);
    return;
  }
  callback(@[[NSNull null], [NSNull null]]);
}

RCT_EXPORT_METHOD(isFingerprintSupported:(RCTResponseSenderBlock)callback){
  BOOL isFingerprintSupported = [DeviceUtil isFingerprintSupported];
  callback(@[[NSNull null], [NSNumber numberWithBool:isFingerprintSupported]]);
}

RCT_EXPORT_METHOD(isLockScreenEnabled:(RCTResponseSenderBlock)callback){
  BOOL isLockScreenEnabled = [DeviceUtil isLockScreenEnabled];
  callback(@[[NSNull null], [NSNumber numberWithBool:isLockScreenEnabled]]);
}

RCT_EXPORT_METHOD(isFingerprintEnrolled:(RCTResponseSenderBlock)callback){
  BOOL isFingerprintEnrolled = [DeviceUtil isFingerprintEnrolled];
  callback(@[[NSNull null], [NSNumber numberWithBool:isFingerprintEnrolled]]);
}

RCT_EXPORT_METHOD(isEligibleForFingerprint:(RCTResponseSenderBlock)callback){
  BOOL isFingerprintSupported = [DeviceUtil isFingerprintSupported];
  BOOL isLockScreenEnabled = [DeviceUtil isLockScreenEnabled];
  BOOL isFingerprintEnrolled = [DeviceUtil isFingerprintEnrolled];
  NSNumber *ret = [NSNumber numberWithBool:NO];
  
  if(isFingerprintSupported && isLockScreenEnabled && isFingerprintEnrolled) {
    ret = [NSNumber numberWithBool:YES];
  }
  callback(@[[NSNull null], ret]);
}

RCT_EXPORT_METHOD(getDeviceName:(RCTResponseSenderBlock)callback){
  NSString *deviceName = [DeviceUtil getDeviceName];
  callback(@[[NSNull null], deviceName]);
}

RCT_EXPORT_METHOD(getDeviceVersion:(RCTResponseSenderBlock)callback){
  NSString *deviceVersion = [DeviceUtil getDeviceVersion];
  callback(@[[NSNull null], deviceVersion]);
}

@end


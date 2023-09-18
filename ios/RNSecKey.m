#import "RNSecKey.h"
#import "DeviceUtil.h"
#import "CryptoUtil.h"
#import "KeyChainUtil.h"

@implementation RNSecKey

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(generateKey:(NSString *)tag :(RCTResponseSenderBlock)callback){
  NSError *error = (NSError *)[CryptoUtil generateKey:tag];
  
  if(error != nil){
    callback(@[[NSNumber numberWithInteger:[error code]], [error localizedDescription]]);
  }
  callback(@[[NSNull null], [CryptoUtil getPublicKey:tag]);
}

RCT_EXPORT_METHOD(getPublicKey:(RCTResponseSenderBlock)callback){
  NSDictionary *publicKeyMap = [CryptoUtil getPublicKeyMap];
  callback(@[[NSNull null], publicKeyMap]);
}

RCT_EXPORT_METHOD(getSignature:(NSString *)keyTag :(NSString *)nonce message:(NSString *) message callback:(RCTResponseSenderBlock)callback){
  NSError *error;
  NSString *signature = [CryptoUtil getSignature:keyTag :nonce message:message error:&error];
  
  if(error != nil){
    callback(@[[NSNumber numberWithInteger:[error code]], signature]);
    return;
  }
  callback(@[[NSNull null], signature]);
}

RCT_EXPORT_METHOD(removeKeyPair:(RCTResponseSenderBlock)callback){
  [CryptoUtil removePrivateKey];
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

RCT_EXPORT_METHOD(saveToKeychain:(NSString *) password account:(NSString *) account identifier:(NSString *) identifier callback:(RCTResponseSenderBlock)callback){
  NSError* error;
  if(password == NULL || account == NULL || identifier == NULL){
    callback(@[[NSNumber numberWithBool:NO], @"missing password / account / identifier"]);
    return;
  }
  BOOL success = [KeyChainUtil saveToKeychain:password account: account identifier:identifier error:&error];
  callback(@[(error != nil ? [NSNumber numberWithInteger:[error code]] : [NSNull null]), [NSNumber numberWithBool:success]]);
}

RCT_EXPORT_METHOD(removeFromKeychain:(NSString *) account identifier:(NSString *) identifier callback:(RCTResponseSenderBlock)callback){
  NSError* error;
  if(account == NULL || identifier == NULL){
    callback(@[[NSNumber numberWithBool:NO], @"missing account / identifier"]);
    return;
  }
  BOOL success = [KeyChainUtil removeFromKeychain:account identifier:identifier error:&error];
  callback(@[(error != nil ? [NSNumber numberWithInteger:[error code]] : [NSNull null]), [NSNumber numberWithBool:success]]);
}

RCT_EXPORT_METHOD(getFromKeychain:(NSString *) account identifier:(NSString *) identifier callback:(RCTResponseSenderBlock)callback){
  if(account == NULL || identifier == NULL){
    callback(@[[NSNumber numberWithBool:NO], @"missing account / identifier"]);
    return;
  }
  NSString *password = [KeyChainUtil getFromKeychain:account identifier:identifier];
  
  callback(@[[NSNull null], password]);
}

RCT_EXPORT_METHOD(saveDeviceId:(NSString *) deviceId callback:(RCTResponseSenderBlock)callback){
  NSError* error;
  BOOL success = [KeyChainUtil saveDeviceId:deviceId error:&error];
  callback(@[(error != nil ? [NSNumber numberWithInteger:[error code]] : [NSNull null]), [NSNumber numberWithBool:success]]);
}

RCT_EXPORT_METHOD(removeDeviceId:(RCTResponseSenderBlock)callback){
  NSError* error;
  BOOL success = [KeyChainUtil removeDeviceId:&error];
  callback(@[(error != nil ? [NSNumber numberWithInteger:[error code]] : [NSNull null]), [NSNumber numberWithBool:success]]);
}

RCT_EXPORT_METHOD(getDeviceId:(RCTResponseSenderBlock)callback){
  NSString *deviceId = [KeyChainUtil getDeviceId];
  if(deviceId == nil){
    deviceId = @"";
  }
  callback(@[[NSNull null], deviceId]);
}

RCT_EXPORT_METHOD(isRooted:(RCTResponseSenderBlock)callback){
  callback(@[[NSNull null], [NSNumber numberWithBool:[DeviceUtil isJailBroken]]]);
}

@end


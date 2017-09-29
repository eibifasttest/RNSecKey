//
//  SecKey.m
//  RNSecKey
//
//  Created by Tan Boon Kiat on 9/28/17.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import "SecKey.h"
#import "CryptoUtil.h"
#import "DeviceUtil.h"
#import "React/RCTLog.h"

@implementation SecKey

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(generateKey:(RCTResponseSenderBlock)callback)
{
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

RCT_EXPORT_METHOD(isEligible:(RCTResponseSenderBlock)callback){
  BOOL isFingerprintSupported = [DeviceUtil isFingerprintSupported];
  BOOL isLockScreenEnabled = [DeviceUtil isLockScreenEnabled];
  BOOL isFingerprintEnrolled = [DeviceUtil isFingerprintEnrolled];
  NSNumber *ret = [NSNumber numberWithBool:NO];
  
  if(isFingerprintSupported && isLockScreenEnabled && isFingerprintEnrolled) {
    ret = [NSNumber numberWithBool:YES];
  }
  callback(@[[NSNull null], ret]);
}


@end

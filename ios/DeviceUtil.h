//
//  DeviceUtil.h
//  RNSecKey
//
//  Created by Tan Boon Kiat on 9/29/17.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <LocalAuthentication/LocalAuthentication.h>

@interface DeviceUtil : NSObject

+ (BOOL)supportSecureEnclave;
+ (BOOL)canEvaluateLAPolicy:(LAPolicy)policy LAError:(LAError)LAError;
+ (BOOL)isLockScreenEnabled;
+ (BOOL)isFingerprintSupported;
+ (BOOL)isFingerprintEnrolled;
+ (NSString *)getDeviceName;
+ (NSString *)getDeviceVersion;
+ (BOOL)isRegisterDeviceToken;
+ (BOOL)isJailBroken;
@end


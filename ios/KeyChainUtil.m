//
//  KeyChainUtil.m
//  RNSecKey
//
//  Created by Tan Boon Kiat on 10/27/17.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import "KeyChainUtil.h"
#import "SAMKeychain.h"
#import "SAMKeychainQuery.h"
#import "Constant.h"

@implementation KeyChainUtil

+ (BOOL) saveToKeychain:(NSString *)data account:(NSString *) account identifier:(NSString *)identifier error:(NSError **) error{
  SAMKeychainQuery *samKeychainQuery = [[SAMKeychainQuery alloc] init];
  [samKeychainQuery setAccount:account];
  [samKeychainQuery setService:identifier];
  [samKeychainQuery setPasswordData:[data dataUsingEncoding:NSUTF8StringEncoding]];
  BOOL status = [samKeychainQuery save:error];
  return status;
}

+ (BOOL) removeFromKeychain:(NSString *) account identifier:(NSString *)identifier error:(NSError **) error{
  BOOL status = NO;
  NSString *deviceId = [SAMKeychain passwordForService:identifier account:account];
  if([deviceId length] != 0){
    NSLog(@"remove device id >>> %@", deviceId);
    SAMKeychainQuery *samKeychainQuery = [[SAMKeychainQuery alloc] init];
    [samKeychainQuery setAccount:account];
    [samKeychainQuery setService:identifier];
    status = [samKeychainQuery deleteItem:error];
    if(!status && error != nil){
      NSLog(@"error removing device id to keychain with error code %ld", [*error code]);
    }
  }
  return status;
}

+ (NSString *) getFromKeychain:(NSString *)account identifier:(NSString *)identifier{
  NSString *password = [SAMKeychain passwordForService:identifier account:account];
  return password;
}

+ (BOOL) saveDeviceId:(NSString *)data error:(NSError *__autoreleasing *)error{
  return [self saveToKeychain:data account:KEYCHAIN_ACCOUNT identifier:DEVICE_ID error:error];
}

+ (BOOL) removeDeviceId:(NSError *__autoreleasing *)error{
  return [self removeFromKeychain:KEYCHAIN_ACCOUNT identifier:DEVICE_ID error:error];
}

+ (NSString *) getDeviceId{
  return [self getFromKeychain:KEYCHAIN_ACCOUNT identifier:DEVICE_ID];
}

@end

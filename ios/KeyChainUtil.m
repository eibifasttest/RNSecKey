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

+ (BOOL) saveToKeychain:(NSString *)data account:(NSMutableString *) account identifier:(NSMutableString *)identifier error:(NSError **) error{
  
  if([account length] == 0){
    account = KEYCHAIN_ACCOUNT;
  }
  
  if([identifier length] == 0){
    identifier = DEVICE_ID;
  }
  
  SAMKeychainQuery *samKeychainQuery = [[SAMKeychainQuery alloc] init];
  [samKeychainQuery setAccount:account];
  [samKeychainQuery setService:identifier];
  [samKeychainQuery setPasswordData:[data dataUsingEncoding:NSUTF8StringEncoding]];
  BOOL status = [samKeychainQuery save:error];
  return status;
}

+ (BOOL) removeFromKeychain:(NSMutableString *) account identifier:(NSMutableString *)identifier error:(NSError **) error{
  BOOL status = NO;
  if([account length] == 0){
    account = KEYCHAIN_ACCOUNT;
  }
  NSString *deviceId = [SAMKeychain passwordForService:DEVICE_ID account:account];
  if([deviceId length] != 0){
    NSLog(@"remove device id >>> %@", deviceId);
    SAMKeychainQuery *samKeychainQuery = [[SAMKeychainQuery alloc] init];
    [samKeychainQuery setAccount:KEYCHAIN_ACCOUNT];
    [samKeychainQuery setService:DEVICE_ID];
    status = [samKeychainQuery deleteItem:error];
    if(!status && error != nil){
      NSLog(@"error removing device id to keychain with error code %ld", [*error code]);
    }
  }
  return status;
}

+ (NSString *) getFromKeychain:(NSMutableString *)account identifier:(NSMutableString *)identifier{
  if([account length] == 0){
    account = KEYCHAIN_ACCOUNT;
  }
  if([identifier length] == 0){
    identifier = DEVICE_ID;
  }
  NSString *password = [SAMKeychain passwordForService:identifier account:account];
  return password;
}

@end

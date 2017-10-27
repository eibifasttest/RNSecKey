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
  NSMutableString *acc = [account mutableCopy];;
  NSMutableString *idty = [identifier mutableCopy];
  if([account length] == 0){
    acc = [KEYCHAIN_ACCOUNT mutableCopy];
  }
  
  if([identifier length] == 0){
    idty = [DEVICE_ID mutableCopy];
  }
  
  SAMKeychainQuery *samKeychainQuery = [[SAMKeychainQuery alloc] init];
  [samKeychainQuery setAccount:acc];
  [samKeychainQuery setService:idty];
  [samKeychainQuery setPasswordData:[data dataUsingEncoding:NSUTF8StringEncoding]];
  BOOL status = [samKeychainQuery save:error];
  return status;
}

+ (BOOL) removeFromKeychain:(NSString *) account identifier:(NSString *)identifier error:(NSError **) error{
  BOOL status = NO;
  NSMutableString *acc = [account mutableCopy];;
  NSMutableString *idty = [identifier mutableCopy];
  if([account length] == 0){
    acc = [KEYCHAIN_ACCOUNT mutableCopy];
  }
  
  if([identifier length] == 0){
    idty = [DEVICE_ID mutableCopy];
  }
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
  NSMutableString *acc = [account mutableCopy];;
  NSMutableString *idty = [identifier mutableCopy];
  if([acc length] == 0){
    acc = [KEYCHAIN_ACCOUNT mutableCopy];
  }
  
  if([idty length] == 0){
    idty = [DEVICE_ID mutableCopy];
  }
  NSString *password = [SAMKeychain passwordForService:idty account:acc];
  return password;
}

@end

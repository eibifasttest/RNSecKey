//
//  CryptoUtil.h
//  RNSecKey
//
//  Created by Tan Boon Kiat on 9/28/17.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CryptoUtil : NSObject
+ (NSObject *)generateKey;
+ (SecKeyRef)getPrivateKey:(NSString *)message;
+ (NSData *) getPublicKeyBitsFromKey:(SecKeyRef)publicKey;
+ (NSString *)transformKey:(NSData *)publicKeyData;
+ (OSStatus)removePrivateKey:(NSString *)keyTag;
+ removePrivateKey;
+ (NSString *)getPublicKey:(NSString *)keyTag;
+ (NSDictionary *)getPublicKeyMap;
+ (NSString *)getSignature:(NSString *)type :(NSString *)nonce message:(NSString *)message error:(NSError **)nsError;
@end

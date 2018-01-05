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
+ (OSStatus)removePrivateKey;
+ (NSString *)getPublicKeyString;
+ (NSString *)getSignature:(NSString *)nonce message:(NSString *)message error:(NSError **)nsError;
+ (id)getPublicKey;
@end

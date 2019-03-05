//
//  SecKeyConstant.m
//  RNSecKey
//
//  Created by Tan Boon Kiat on 9/29/17.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import "SecKeyConstant.h"

@implementation SecKeyConstant

NSString * const PUBLIC_KEY_TAG = @"com.RNPlugin.RNSecKey.cryptopublic";
NSString * const PRIVATE_KEY_TAG = @"com.RNPlugin.RNSecKey.cryptoprivate";

NSString * const SIGN_PUBLIC_KEY_TAG = @"com.RNPlugin.RNSecKey.signcryptopublic";
NSString * const SIGN_PRIVATE_KEY_TAG = @"com.RNPlugin.RNSecKey.signcryptoprivate";

NSString * const AUTHENTICATE = @"AUTHENTICATE";
NSString * const SIGNING = @"SIGNING";

NSString * const BIOMETRIC_LOCKOUT = @"BIOMETRIC_LOCKOUT";
NSString * const BIOMETRIC_FAILED_CONSECUTIVE = @"BIOMETRIC_FAILED_CONSECUTIVE";
NSString * const BIOMETRIC_DIFFERENT_STATE = @"BIOMETRIC_DIFFERENT_STATE";
NSString * const BIOMETRIC_CANCEL = @"BIOMETRIC_CANCEL";

@end

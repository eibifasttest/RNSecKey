//
//  SecKeyConstant.h
//  RNSecKey
//
//  Created by Tan Boon Kiat on 9/29/17.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SecKeyConstant : NSObject

extern NSString * const PUBLIC_KEY_TAG;
extern NSString * const PRIVATE_KEY_TAG;

extern NSString * const BIOMETRIC_LOCKOUT;
extern NSString * const BIOMETRIC_FAILED_CONSECUTIVE;
extern NSString * const BIOMETRIC_DIFFERENT_STATE;
extern NSString * const BIOMETRIC_CANCEL;

@end

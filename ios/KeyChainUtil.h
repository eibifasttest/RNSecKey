//
//  KeyChainUtil.h
//  RNSecKey
//
//  Created by Tan Boon Kiat on 10/27/17.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface KeyChainUtil : NSObject

+ (BOOL) saveToKeychain:(NSString *) data account:(NSString *)account identifier:(NSString *) identifier error:(NSError **) error;
+ (BOOL) removeFromKeychain:(NSString *) account identifier:(NSString *) identifier error:(NSError **) error;
+ (NSString *) getFromKeychain:(NSString *)account identifier:(NSString *)identifier;

@end

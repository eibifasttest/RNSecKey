//
//  KeyChainUtil.h
//  RNSecKey
//
//  Created by Tan Boon Kiat on 10/27/17.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface KeyChainUtil : NSObject

+ (BOOL) saveToKeychain:(NSString *) data account:(NSMutableString *)account identifier:(NSMutableString *) identifier error:(NSError **) error;
+ (BOOL) removeFromKeychain:(NSMutableString *) account identifier:(NSMutableString *) identifier error:(NSError **) error;
+ (NSString *) getFromKeychain:(NSMutableString *)account identifier:(NSMutableString *)identifier;

@end

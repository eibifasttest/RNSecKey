//
//  DeviceUtil.m
//  RNSecKey
//
//  Created by Tan Boon Kiat on 9/29/17.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import "DeviceUtil.h"
#include <sys/sysctl.h>

@implementation DeviceUtil

+ (BOOL)supportSecureEnclave{
  NSString *thisPlatform = [self platform];
  
  NSArray * oldModels = [NSArray arrayWithObjects:
                         @"x86_64",
                         @"iPhone1,1",
                         @"iPhone1,2",
                         @"iPhone2,1",
                         @"iPhone3,1",
                         @"iPhone3,3",
                         @"iPhone4,1",
                         @"iPhone5,1",
                         @"iPhone5,2",
                         @"iPhone5,3",
                         @"iPhone5,4",
                         @"@iPad",
                         @"@iPad1,0",
                         @"@iPad1,1",
                         @"iPad2,1",
                         @"iPad2,2",
                         @"iPad2,3",
                         @"iPad2,4",
                         @"iPad2,5",
                         @"iPad2,6",
                         @"iPad2,7",
                         @"iPad3,1",
                         @"iPad3,2",
                         @"iPad3,3",
                         @"iPad3,4",
                         @"iPad3,5",
                         @"iPad3,6",nil];
  
  return ![oldModels containsObject: thisPlatform];
}

+ (NSString *)platform
{
  size_t size;
  sysctlbyname("hw.machine", NULL, &size, NULL, 0);
  char *machine = malloc(size);
  sysctlbyname("hw.machine", machine, &size, NULL, 0);
  NSString *platform = [NSString stringWithUTF8String:machine];
  free(machine);
  
  return platform;
  
}

+ (BOOL)canEvaluateLAPolicy:(LAPolicy)policy LAError:(LAError)LAError{
  LAContext *context = [[LAContext alloc] init];
  NSError *error;
  BOOL success;
  
  success = [context canEvaluatePolicy: policy error:&error];
  NSLog(@"policy error: %ld", (long)error.code);
  if(LAError != 0){
    if(error.code == LAError){
      return YES;
    }
  }
  
  if (success && error.code == 0) {
    NSLog(@"i am success!!!!!");
    return YES;
    
  }
  
  return NO;
}

+ (BOOL)isLockScreenEnabled{
  return [self canEvaluateLAPolicy:kLAPolicyDeviceOwnerAuthentication LAError:0];
}

+ (BOOL)isFingerprintSupported{
  return [self canEvaluateLAPolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics LAError:LAErrorTouchIDNotEnrolled];
}

+ (BOOL)isFingerprintEnrolled{
  return [self canEvaluateLAPolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics LAError:0];
}

@end

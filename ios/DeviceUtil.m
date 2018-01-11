//
//  DeviceUtil.m
//  RNSecKey
//
//  Created by Tan Boon Kiat on 9/29/17.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import "DeviceUtil.h"
#import <UIKit/UIKit.h>
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
  if (@available(iOS 11.0, *)) {
    return [self canEvaluateLAPolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics LAError:LAErrorBiometryNotEnrolled];
  } else {
    return [self canEvaluateLAPolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics LAError:LAErrorTouchIDNotEnrolled];
  }
}

+ (BOOL)isFingerprintEnrolled{
  return [self canEvaluateLAPolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics LAError:0];
}

+ (NSString *)getDeviceName{
  return [[UIDevice currentDevice] name];
}

+ (NSString *)getDeviceVersion{
  return [[UIDevice currentDevice] systemVersion];
}

+ (BOOL)isRegisterDeviceToken{
  return [[UIApplication sharedApplication] isRegisteredForRemoteNotifications];
}

+ (BOOL)isJailBroken{
  //source https://medium.com/@pinmadhon/how-to-check-your-app-is-installed-on-a-jailbroken-device-67fa0170cf56 (may 2017)
#if !(TARGET_IPHONE_SIMULATOR)
  // Check 1 : existence of files that are common for jailbroken devices
  if ([[NSFileManager defaultManager] fileExistsAtPath:@"/Applications/Cydia.app"] ||
      [[NSFileManager defaultManager] fileExistsAtPath:@"/Library/MobileSubstrate/MobileSubstrate.dylib"] ||
      [[NSFileManager defaultManager] fileExistsAtPath:@"/bin/bash"] ||
      [[NSFileManager defaultManager] fileExistsAtPath:@"/usr/sbin/sshd"] ||
      [[NSFileManager defaultManager] fileExistsAtPath:@"/etc/apt"] ||
      [[NSFileManager defaultManager] fileExistsAtPath:@"/private/var/lib/apt/"] ||
      [[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"cydia://package/com.example.package"]]) {
    return YES;
  }
  FILE *f = NULL ;
  if ((f = fopen("/bin/bash", "r")) ||
      (f = fopen("/Applications/Cydia.app", "r")) ||
      (f = fopen("/Library/MobileSubstrate/MobileSubstrate.dylib", "r")) ||
      (f = fopen("/usr/sbin/sshd", "r")) ||
      (f = fopen("/etc/apt", "r"))) {
    fclose(f);
    return YES;
  }
  fclose(f);
  // Check 2 : Reading and writing in system directories (sandbox violation)
  NSError *error;
  NSString *stringToBeWritten = @"Jailbreak Test.";
  [stringToBeWritten writeToFile:@"/private/jailbreak.txt" atomically:YES
                        encoding:NSUTF8StringEncoding error:&error];
  if(error==nil){
    //Device is jailbroken
    return YES;
  } else {
    [[NSFileManager defaultManager] removeItemAtPath:@"/private/jailbreak.txt" error:nil];
  }
#endif
  return NO;
}

@end



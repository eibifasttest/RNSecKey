/**
 * @providesModule RNSecKey
 * @flow
 */
import ReactNative from "react-native";

const { NativeModules } = ReactNative;
const NativeRNSecKey = NativeModules.RNSecKey;

/**
 * High-level docs for the RNSecKey iOS API can be written here.
 */

var RNSecKey = {
  test: function() {
    NativeRNSecKey.test();
  },
  generateKey: callback => NativeRNSecKey.generateKey(callback),
  getPublicKey: callback => NativeRNSecKey.getPublicKey(callback),
  getSignature: (type, nonce, message, callback) => {
    NativeRNSecKey.getSignature(type, nonce, message, callback);
  },
  removeKeyPair: callback => NativeRNSecKey.removeKeyPair(callback),
  isFingerprintSupported: callback =>
    NativeRNSecKey.isFingerprintSupported(callback),
  isLockScreenEnabled: callback => NativeRNSecKey.isLockScreenEnabled(callback),
  isEligibleForFingerprint: callback =>
    NativeRNSecKey.isEligibleForFingerprint(callback),
  getDeviceName: callback => NativeRNSecKey.getDeviceName(callback),
  getDeviceVersion: callback => NativeRNSecKey.getDeviceVersion(callback),
  getDeviceId: callback => NativeRNSecKey.getDeviceId(callback),
  saveDeviceId: (deviceId, callback) =>
    NativeRNSecKey.saveDeviceId(deviceId, callback),
  removeDeviceId: callback => NativeRNSecKey.removeDeviceId(callback),
  saveToKeychain: (password, account, identifier, callback) =>
    NativeRNSecKey.saveToKeychain(password, account, identifier, callback),
  getFromKeychain: (account, identifier, callback) =>
    NativeRNSecKey.getFromKeychain(account, identifier, callback),
  removeFromKeychain: (account, identifier, callback) =>
    NativeRNSecKey.removeFromKeychain(account, identifier, callback),
  isRooted: callback => NativeRNSecKey.isRooted(callback)
};

export default RNSecKey;

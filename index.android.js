 /**
 * @providesModule RNSecKey
 * @flow
 */
import ReactNative from 'react-native';

const { NativeModules } = ReactNative;
const NativeRNSecKey = NativeModules.RNSecKey;

/**
 * High-level docs for the RNSecKey iOS API can be written here.
 */

var RNSecKey = {
  test: function() {
    NativeRNSecKey.test();
  },
  generateKey: (callback) => NativeRNSecKey.generateKey(callback),
  getPublicKey: (callback) => NativeRNSecKey.getPublicKey(callback),
  getSignature: (nonce, callback) => NativeRNSecKey.getSignature(nonce, callback),
  removeKeyPair: (callback) => NativeRNSecKey.removeKeyPair(callback),
  isFingerprintSupported: (callback) => NativeRNSecKey.isFingerprintSupported(callback),
  isLockScreenEnabled: (callback) => NativeRNSecKey.isLockScreenEnabled(callback),
  isEligibleForFingerprint: (callback) => NativeRNSecKey.isEligibleForFingerprint(callback),
  getDeviceName: (callback) => NativeRNSecKey.getDeviceName(callback),
  getDeviceVersion: (callback) => NativeRNSecKey.getDeviceVersion(callback),

};

export default RNSecKey;

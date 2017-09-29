/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

 import ReactNative from 'react-native';

 const { NativeModules } = ReactNative;
 const SecKey = NativeModules.SecKey;

 export function generateKey(callback) {
   SecKey.generateKey(callback);
 }

 export function getPublicKey(callback) {
   SecKey.getPublicKey(callback);
 }

 export function getSignature(nonce, callback) {
   SecKey.getSignature(nonce, callback);
 }

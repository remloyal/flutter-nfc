import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'nfcpkg_platform_interface.dart';

/// An implementation of [NfcpkgPlatform] that uses method channels.
class MethodChannelNfcpkg extends NfcpkgPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('nfcpkg');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<bool> poll() async {
    var data = await methodChannel.invokeMethod('poll');
    return data;
  }

  @override
  Future<String> transceive<String>(String capdu) async {
    int technologies = 0x8;
    // if (readIso14443A) technologies |= 0x1;
    // if (readIso14443B) technologies |= 0x2;
    // if (readIso18092) technologies |= 0x4;
    // if (readIso15693) technologies |= 0x8;
    int pollTimeout = 20 * 1000;
    var data = await methodChannel.invokeMethod('transceive', {
      'data': capdu,
      "technologies": technologies,
      'timeout': pollTimeout,
    });
    return data;
  }
}

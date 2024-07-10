import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'nfcpkg_method_channel.dart';

abstract class NfcpkgPlatform extends PlatformInterface {
  /// Constructs a NfcpkgPlatform.
  NfcpkgPlatform() : super(token: _token);

  static final Object _token = Object();

  static NfcpkgPlatform _instance = MethodChannelNfcpkg();

  /// The default instance of [NfcpkgPlatform] to use.
  ///
  /// Defaults to [MethodChannelNfcpkg].
  static NfcpkgPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [NfcpkgPlatform] when
  /// they register themselves.
  static set instance(NfcpkgPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String> transceive<String>(String capdu) {
    throw UnimplementedError('transceive() has not been implemented.');
  }
  Future<bool> poll() {
    throw UnimplementedError('poll() has not been implemented.');
  }
}



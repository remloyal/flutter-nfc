import 'package:flutter_test/flutter_test.dart';
import 'package:nfcpkg/nfcpkg.dart';
import 'package:nfcpkg/nfcpkg_platform_interface.dart';
import 'package:nfcpkg/nfcpkg_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockNfcpkgPlatform
    with MockPlatformInterfaceMixin
    implements NfcpkgPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final NfcpkgPlatform initialPlatform = NfcpkgPlatform.instance;

  test('$MethodChannelNfcpkg is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelNfcpkg>());
  });

  test('getPlatformVersion', () async {
    Nfcpkg nfcpkgPlugin = Nfcpkg();
    MockNfcpkgPlatform fakePlatform = MockNfcpkgPlatform();
    NfcpkgPlatform.instance = fakePlatform;

    expect(await nfcpkgPlugin.getPlatformVersion(), '42');
  });
}

import 'nfcpkg_platform_interface.dart';

class Nfcpkg {
  Future<String?> getPlatformVersion() {
    return NfcpkgPlatform.instance.getPlatformVersion();
  }

  Future<bool> poll() async {
    return await NfcpkgPlatform.instance.poll();
  }

  Future<String> transceive(String str) async {
    return await NfcpkgPlatform.instance.transceive(str);
  }
}

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

MethodChannel? androidViewChannel;

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  Future<dynamic> _handleMethodCall(MethodCall call) async {
    await Future.delayed(const Duration(milliseconds: 10));
    print("[LANGOHA][_handleMethodCall] Received method call: ${call.method}");
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Lango HA',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: Scaffold(
        body: AndroidView(
          viewType: 'custom_canvas_view',
          creationParams: {},
          creationParamsCodec: StandardMessageCodec(),
          onPlatformViewCreated: (int id) {
            print("[LANGOHA][onPlatformViewCreated] Trying to create Platform Channel");
            androidViewChannel = MethodChannel('custom_canvas_view_$id');
            androidViewChannel?.setMethodCallHandler(_handleMethodCall);
          },
        ),
      ),
    );
  }
}

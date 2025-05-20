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
  static const MethodChannel _channel = MethodChannel('com.example.lango_ha/native');
  bool isAndroidViewWhiteboardVisible = false;

  Future<void> launchNativeWhiteboard() async {
    try {
      await _channel.invokeMethod('launchWhiteboard');
    } catch (e) {
      print('Error launching native whiteboard: $e');
    }
  }

  void androidViewWhiteboardVisible(BuildContext context) {
    setState(() {
      isAndroidViewWhiteboardVisible = true;
    });
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
        body: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              ElevatedButton(
                onPressed: launchNativeWhiteboard,
                child: const Text('Open Native Whiteboard'),
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () => androidViewWhiteboardVisible(context),
                child: const Text('Open Android View Whiteboard'),
              ),
              const SizedBox(height: 16),
              if (isAndroidViewWhiteboardVisible)
                Container(
                  width: 320,
                  height: 180,
                  decoration: BoxDecoration(
                    border: Border.all(color: Colors.blue),
                  ),
                  child: AndroidView(
                    viewType: 'custom_canvas_view',
                    creationParams: const {},
                    creationParamsCodec: const StandardMessageCodec(),
                    onPlatformViewCreated: (int id) {
                      print("[LANGOHA][onPlatformViewCreated] Trying to create Platform Channel");
                      // androidViewChannel = MethodChannel('custom_canvas_view_$id');
                      // androidViewChannel?.setMethodCallHandler(_handleMethodCall);
                    },
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}

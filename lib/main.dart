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
      isAndroidViewWhiteboardVisible = !isAndroidViewWhiteboardVisible;
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
        body: Stack(
          alignment: Alignment.center,
          children: [
            Container(
              width: MediaQuery.of(context).size.width * 0.8,
              height: MediaQuery.of(context).size.height * 0.8,
              decoration: BoxDecoration(
                border: Border.all(color: Colors.blue),
              ),
              child: isAndroidViewWhiteboardVisible
                  ? AndroidView(
                      viewType: 'custom_canvas_view',
                      creationParams: const {},
                      creationParamsCodec: const StandardMessageCodec(),
                      onPlatformViewCreated: (int id) {
                        print("[LANGOHA][onPlatformViewCreated] Trying to create Platform Channel");
                        // androidViewChannel = MethodChannel('custom_canvas_view_$id');
                        // androidViewChannel?.setMethodCallHandler(_handleMethodCall);
                      },
                    )
                  : const SizedBox.shrink(),
            ),
            SizedBox(
              width: MediaQuery.of(context).size.width,
              height: MediaQuery.of(context).size.height,
              child: Align(
                alignment: Alignment.bottomCenter,
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    ElevatedButton(
                      onPressed: launchNativeWhiteboard,
                      child: const Text('Open Native Whiteboard'),
                    ),
                    const SizedBox(width: 16),
                    ElevatedButton(
                      onPressed: () => androidViewWhiteboardVisible(context),
                      child: Text('Open Android View Whiteboard ${isAndroidViewWhiteboardVisible ? 'Visible' : 'Hidden'}'),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

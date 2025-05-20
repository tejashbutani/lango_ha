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

  Future<void> launchNativeWhiteboard() async {
    try {
      await _channel.invokeMethod('launchWhiteboard');
    } catch (e) {
      print('Error launching native whiteboard: $e');
    }
  }

  void openAndroidViewWhiteboard(BuildContext context) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => const AndroidViewWhiteboardPage(),
      ),
    );
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
              ElevatedButton(
                onPressed: () => openAndroidViewWhiteboard(context),
                child: const Text('Open Android View Whiteboard'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class AndroidViewWhiteboardPage extends StatelessWidget {
  const AndroidViewWhiteboardPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Android View Whiteboard')),
      body: const AndroidView(
        viewType: 'custom_canvas_view',
        creationParams: {},
        creationParamsCodec: StandardMessageCodec(),
      ),
    );
  }
}

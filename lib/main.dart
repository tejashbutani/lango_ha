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
          child: ElevatedButton(
            onPressed: launchNativeWhiteboard,
            child: const Text('Open Native Whiteboard'),
          ),
        ),
      ),
    );
  }
}



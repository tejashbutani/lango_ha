import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/widgets.dart';

void main() {
  runApp(const MyApp());
}

MethodChannel? androidViewChannel;

class MyPlatformView extends StatelessWidget {
  const MyPlatformView({super.key});

  @override
  Widget build(BuildContext context) {
    return PlatformViewLink(
      viewType: 'custom_canvas_view',
      surfaceFactory: (context, controller) {
        return AndroidViewSurface(
          controller: controller as AndroidViewController,
          gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
          hitTestBehavior: PlatformViewHitTestBehavior.opaque,
        );
      },
      onCreatePlatformView: (params) {
        final controller = PlatformViewsService.initSurfaceAndroidView(
          id: params.id,
          viewType: 'custom_canvas_view',
          layoutDirection: TextDirection.ltr,
          creationParams: const <String, dynamic>{},
          creationParamsCodec: const StandardMessageCodec(),
        );
        controller.addOnPlatformViewCreatedListener(params.onPlatformViewCreated);
        controller.create();
        return controller;
      },
    );
  }
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  static const MethodChannel _channel = MethodChannel('com.example.lango_ha/native');
  bool isAndroidViewWhiteboardVisible = false;
  bool isPlatformViewLinkWhiteboardVisible = false;
  bool isAndroidViewSurfaceVisible = false;

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
      if (isAndroidViewWhiteboardVisible) {
        isPlatformViewLinkWhiteboardVisible = false;
        isAndroidViewSurfaceVisible = false;
      }
    });
  }

  void platformViewLinkWhiteboardVisible(BuildContext context) {
    setState(() {
      isPlatformViewLinkWhiteboardVisible = !isPlatformViewLinkWhiteboardVisible;
      if (isPlatformViewLinkWhiteboardVisible) {
        isAndroidViewWhiteboardVisible = false;
        isAndroidViewSurfaceVisible = false;
      }
    });
  }

  void androidViewSurfaceVisible(BuildContext context) {
    setState(() {
      isAndroidViewSurfaceVisible = !isAndroidViewSurfaceVisible;
      if (isAndroidViewSurfaceVisible) {
        isAndroidViewWhiteboardVisible = false;
        isPlatformViewLinkWhiteboardVisible = false;
      }
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
                      },
                    )
                  : isPlatformViewLinkWhiteboardVisible
                      ? const MyPlatformView()
                      : isAndroidViewSurfaceVisible
                          ? PlatformViewLink(
                              viewType: 'custom_canvas_view',
                              surfaceFactory: (context, controller) {
                                return AndroidViewSurface(
                                  controller: controller as AndroidViewController,
                                  gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
                                  hitTestBehavior: PlatformViewHitTestBehavior.opaque,
                                );
                              },
                              onCreatePlatformView: (params) {
                                return PlatformViewsService.initSurfaceAndroidView(
                                  id: params.id,
                                  viewType: 'custom_canvas_view',
                                  layoutDirection: TextDirection.ltr,
                                  creationParams: const {},
                                  creationParamsCodec: const StandardMessageCodec(),
                                )
                                  ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
                                  ..create();
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
                    const SizedBox(width: 16),
                    ElevatedButton(
                      onPressed: () => platformViewLinkWhiteboardVisible(context),
                      child: Text('Open PlatformViewLink Whiteboard ${isPlatformViewLinkWhiteboardVisible ? 'Visible' : 'Hidden'}'),
                    ),
                    const SizedBox(width: 16),
                    ElevatedButton(
                      onPressed: () => androidViewSurfaceVisible(context),
                      child: Text('Open AndroidViewSurface Whiteboard ${isAndroidViewSurfaceVisible ? 'Visible' : 'Hidden'}'),
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

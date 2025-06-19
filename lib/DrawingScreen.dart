import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

class DrawingScreen extends StatefulWidget {
  const DrawingScreen({super.key});

  @override
  State<DrawingScreen> createState() => _DrawingScreenState();
}

class _DrawingScreenState extends State<DrawingScreen> {
  MethodChannel? androidViewChannel;
  bool isPenEnabled = false;
  List<Stroke> strokes = [];
  Size? androidViewSize;

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        androidViewSize = Size(constraints.maxWidth, constraints.maxHeight);

        return Scaffold(
          backgroundColor: Colors.grey.shade600,
          body: Stack(
            children: [
              
              if (isPenEnabled)
                AndroidView(
                  viewType: 'custom_canvas_view',
                  creationParams: {
                     'color': Colors.white.value,
                          'width': 3,
                          "doublePenModeEnabled": false,
                          "doublePenColor1": Colors.red.value,
                          "doublePenColor2": Colors.blue.value,
                          "fingerAsEraserEnabled": false,
                          // "fingerAsEraserEnabled": true,
                          "fistAsEraserEnabled": false,
                          "fistAsEraserThreshold": 1000,
                          "fingerAsEraserThreshold": 1000,
                          "doublePenThreshold": 1000,
                  },
                  creationParamsCodec: const StandardMessageCodec(),
                  onPlatformViewCreated: (int id) {
                    print("Trying to create Platform Channel");
                    // if (!_androidViewCreated) {
                    print("Trying to create Platform Channel");
                    androidViewChannel = MethodChannel('custom_canvas_view_$id');
                    androidViewChannel?.setMethodCallHandler(_handleMethodCall);
                    // _androidViewCreated = true;
                    // }
                  },
                ),
                CustomPaint(
                painter: ToolsPainter(
                  strokes: strokes,
                  androidViewSize: androidViewSize,
                ),
                size: const Size(3860, 2160),
              ),
              Positioned(
                bottom: 40,
                right: 120,
                child: FloatingActionButton(
                  onPressed: () {
                    setState(() {
                      isPenEnabled = !isPenEnabled;
                    });
                  },
                  backgroundColor: isPenEnabled ? Colors.black : Colors.white,
                  child: Icon(
                    isPenEnabled ? Icons.edit : Icons.edit_off,
                    color: isPenEnabled ? Colors.red : Colors.red,
                  ),
                ),
              ),
              Positioned(
                bottom: 40,
                right: 40,
                child: FloatingActionButton(
                  onPressed: () {
                    setState(() {
                      strokes.clear();
                    });
                  },
                  backgroundColor: Colors.white,
                  child: const Icon(
                    Icons.delete_outline,
                    color: Colors.red,
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Future<dynamic> _handleMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'onStrokeComplete':
        try {
          final strokeData = Map<String, dynamic>.from(call.arguments);
          final stroke = Stroke.fromJson(strokeData);
          setState(() {
            strokes.add(stroke);
          });
          print('Received stroke with ${stroke.points.length} points'); // Debug log
        } catch (e) {
          print('Error processing stroke data: $e'); // Debug log
        }
        break;
    }
  }
}

class ToolsPainter extends CustomPainter {
  final List<Stroke> strokes;
  final Size? androidViewSize;

  ToolsPainter({
    required this.strokes,
    this.androidViewSize,
  });

  @override
  void paint(Canvas canvas, Size size) {
    for (final stroke in strokes) {
      if (stroke.points.length < 2) continue;

      final paint = Paint()
        ..color = stroke.color
        ..strokeWidth = stroke.width
        ..strokeCap = StrokeCap.round
        ..strokeJoin = StrokeJoin.round
        ..style = PaintingStyle.stroke;

      if (stroke is DashedStroke) {
        final path = Path();
        path.moveTo(stroke.points[0].dx, stroke.points[0].dy);
        for (int i = 1; i < stroke.points.length; i++) {
          path.lineTo(stroke.points[i].dx, stroke.points[i].dy);
        }

        final pathMetrics = path.computeMetrics();
        final dashedPath = Path();

        for (final metric in pathMetrics) {
          var distance = 0.0;
          final length = metric.length;

          while (distance < length) {
            // Draw dash
            dashedPath.addPath(
              metric.extractPath(distance, distance + 30),
              Offset.zero,
            );
            // Skip gap
            distance += 50; // 30 (dash) + 20 (gap)
          }
        }

        canvas.drawPath(dashedPath, paint);
      } else {
        final path = Path();
        path.moveTo(stroke.points[0].dx, stroke.points[0].dy);

        for (int i = 1; i < stroke.points.length; i++) {
          path.lineTo(stroke.points[i].dx, stroke.points[i].dy);
        }

        canvas.drawPath(path, paint);
      }
    }
  }

  @override
  bool shouldRepaint(ToolsPainter oldDelegate) {
    return true;
  }
}

class Stroke {
  List<Offset> points;
  final Color color;
  final double width;
  final bool isDashed;

  Stroke({
    required this.points,
    this.color = Colors.black,
    this.width = 5.0,
    this.isDashed = false,
  });

  Map<String, dynamic> toJson() {
    return {
      'points': points.map((p) => {'x': p.dx, 'y': p.dy}).toList(),
      'color': color.value,
      'width': width,
      'isDashed': isDashed,
    };
  }

  factory Stroke.fromJson(Map<String, dynamic> json) {
    return Stroke(
      points: (json['points'] as List).map((p) => Offset(p['x'] as double, p['y'] as double)).toList(),
      color: Color(json['color'] as int),
      width: json['width'] as double,
      isDashed: json['isDashed'] as bool? ?? false,
    );
  }
}

class DashedStroke extends Stroke {
  DashedStroke({
    required super.points,
    super.color,
    super.width,
  }) : super(
          isDashed: true,
        );
}

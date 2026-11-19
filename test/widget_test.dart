import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:tp3_channels/main.dart';

void main() {
  testWidgets('La page affiche le titre du TP', (WidgetTester tester) async {
    await tester.pumpWidget(const MaterialApp(home: PageCanaux()));
    expect(find.text('TP 3 – Platform Channels'), findsOneWidget);
  });
}

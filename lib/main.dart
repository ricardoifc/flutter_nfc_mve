import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});


  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  String _nfcData = "Esperando datos...";

  // Método para manejar las actualizaciones desde Android nativo
  Future<void> _onNFCDataChanged(String data) async {
    setState(() {
      List<String> parts = data.split("|");
      String idContentString = parts[0];
      String processState = parts[1];
      _nfcData = "=> $idContentString\nEstado: $processState";

    });
  }

  @override
  void initState() {
    super.initState();
    // Inicializar el MethodChannel para recibir actualizaciones
    MethodChannel channel = MethodChannel("com.example/nfc_channel");
    channel.setMethodCallHandler((call) {
      if (call.method == "onNFCDataChanged") {
        String data = call.arguments as String;
        _onNFCDataChanged(data);
      }
      return Future.value(null);
    });
  }


   @override
  Widget build(BuildContext context) {

    return Scaffold(
      appBar: AppBar(

        backgroundColor: Theme.of(context).colorScheme.inversePrimary,

        title: Text(''),
      ),
      body: Center(
        child: Text(_nfcData),
      ),

    );
  }
}

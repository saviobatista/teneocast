import 'dart:async';
import 'dart:convert';
import 'dart:developer';
import 'package:web_socket_channel/web_socket_channel.dart';
import '../constants/app_constants.dart';
import '../utils/error_handler.dart';

class WebSocketService {
  WebSocketChannel? _channel;
  StreamController<Map<String, dynamic>>? _messageController;
  Timer? _reconnectTimer;
  bool _isConnected = false;
  int _reconnectAttempts = 0;
  static const int _maxReconnectAttempts = 5;

  Stream<Map<String, dynamic>> get messageStream => _messageController!.stream;
  bool get isConnected => _isConnected;

  Future<void> connect({String? token}) async {
    try {
      final uri = Uri.parse('${AppConstants.wsBaseUrl}/player');
      final wsUrl = token != null ? '$uri?token=$token' : uri.toString();
      
      _channel = WebSocketChannel.connect(Uri.parse(wsUrl));
      _messageController = StreamController<Map<String, dynamic>>.broadcast();

      _channel!.stream.listen(
        (data) {
          if (data is String) {
            try {
              final jsonData = json.decode(data) as Map<String, dynamic>;
              _messageController!.add(jsonData);
            } catch (e) {
              // Handle non-JSON messages
              _messageController!.add({'type': 'message', 'data': data});
            }
          } else if (data is Map<String, dynamic>) {
            _messageController!.add(data);
          }
        },
        onError: (error) {
          _isConnected = false;
          _handleConnectionError(error);
        },
        onDone: () {
          _isConnected = false;
          _scheduleReconnect();
        },
      );

      _isConnected = true;
      _reconnectAttempts = 0;
    } catch (e) {
      _isConnected = false;
      throw NetworkException('Failed to connect to WebSocket: $e');
    }
  }

  void send(Map<String, dynamic> message) {
    if (_channel != null && _isConnected) {
      _channel!.sink.add(json.encode(message));
    } else {
      throw NetworkException('WebSocket is not connected');
    }
  }

  void sendCommand(String playerId, String commandType, {Map<String, dynamic>? payload}) {
    send({
      'type': 'COMMAND',
      'playerId': playerId,
      'commandType': commandType,
      'payload': payload,
      'timestamp': DateTime.now().toIso8601String(),
    });
  }

  void sendTTS(String playerId, String text) {
    send({
      'type': 'TTS',
      'playerId': playerId,
      'text': text,
      'timestamp': DateTime.now().toIso8601String(),
    });
  }

  void _handleConnectionError(dynamic error) {
    // Log error and attempt reconnection
    // TODO: Replace with proper logging service
    log('WebSocket error: $error');
    _scheduleReconnect();
  }

  void _scheduleReconnect() {
    if (_reconnectAttempts < _maxReconnectAttempts) {
      _reconnectTimer?.cancel();
      _reconnectTimer = Timer(
        Duration(seconds: _reconnectAttempts + 1),
        () => _attemptReconnect(),
      );
    }
  }

  Future<void> _attemptReconnect() async {
    _reconnectAttempts++;
    try {
      await connect();
    } catch (e) {
      if (_reconnectAttempts < _maxReconnectAttempts) {
        _scheduleReconnect();
      }
    }
  }

  void disconnect() {
    _reconnectTimer?.cancel();
    _channel?.sink.close();
    _messageController?.close();
    _isConnected = false;
    _reconnectAttempts = 0;
  }

  void dispose() {
    disconnect();
  }
} 
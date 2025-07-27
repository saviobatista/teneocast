import 'package:flutter/material.dart';
import '../constants/app_constants.dart';

class ErrorHandler {
  static String getErrorMessage(dynamic error) {
    if (error is NetworkException) {
      return AppConstants.networkErrorMessage;
    } else if (error is ServerException) {
      return AppConstants.serverErrorMessage;
    } else if (error is TimeoutException) {
      return AppConstants.timeoutErrorMessage;
    } else {
      return AppConstants.unknownErrorMessage;
    }
  }

  static void showErrorSnackBar(BuildContext context, String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.red,
        behavior: SnackBarBehavior.floating,
        margin: const EdgeInsets.all(16),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(8),
        ),
      ),
    );
  }

  static void showSuccessSnackBar(BuildContext context, String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.green,
        behavior: SnackBarBehavior.floating,
        margin: const EdgeInsets.all(16),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(8),
        ),
      ),
    );
  }

  static void showWarningSnackBar(BuildContext context, String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.orange,
        behavior: SnackBarBehavior.floating,
        margin: const EdgeInsets.all(16),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(8),
        ),
      ),
    );
  }
}

class NetworkException implements Exception {
  final String message;
  NetworkException([this.message = 'Network error occurred']);
}

class ServerException implements Exception {
  final String message;
  ServerException([this.message = 'Server error occurred']);
}

class TimeoutException implements Exception {
  final String message;
  TimeoutException([this.message = 'Request timed out']);
} 
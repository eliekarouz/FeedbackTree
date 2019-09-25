package com.feedbacktree.flow.utils

import android.util.Log

/**
 * In case you want to have your custom logger, feel free to implement this interface and
 * set [logger] to the what you have created.
 * You can also set the [logLevel] that fits your needs. The default value is [LogLevel.Info]
 */
interface Logger {
    fun info(message: String)
    fun debug(message: String)
    fun verbose(message: String)
    fun error(message: String)
}

enum class LogLevel {
    Error, Info, Debug, Verbose
}

var logLevel: LogLevel = LogLevel.Verbose
var logger: Logger = ConsoleLogger()


private class ConsoleLogger : Logger {

    override fun error(message: String) {
        Log.e("FT", message)
    }

    override fun info(message: String) {
        Log.i("FT", message)
    }

    override fun debug(message: String) {
        Log.d("FT", message)
    }

    override fun verbose(message: String) {
        Log.v("FT", message)
    }
}


internal fun logError(message: String) {
    if (logLevel >= LogLevel.Error) {
        logger.error(message)
    }
}

internal fun logInfo(message: String) {
    if (logLevel >= LogLevel.Info) {
        logger.info(message)
    }
}

internal fun logDebug(message: String) {
    if (logLevel >= LogLevel.Debug) {
        logger.debug(message)
    }
}

internal fun logVerbose(message: String) {
    if (logLevel >= LogLevel.Verbose) {
        logger.verbose(message)
    }
}



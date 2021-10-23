package com.xmlcalabash.gradle

trait XmlCalabashPluginOptions {
  abstract void setOption(String name, Object value)
  abstract void setPluginOption(String name, Object value)

  // ============================================================

  void debugPlugin(Boolean debug) {
    setPluginOption('debug', debug)
  }

  void debug(Boolean debug) {
    setOption('debug', debug)
  }

  void verbose(Boolean verbose) {
    setOption('verbose', verbose)
  }

  void graph(Object name) {
    setOption('graph', name)
  }

  void stacktrace(Boolean trace) {
    setOption('stacktrace', trace)
  }
}

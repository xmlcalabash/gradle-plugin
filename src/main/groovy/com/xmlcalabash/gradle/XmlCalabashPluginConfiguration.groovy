package com.xmlcalabash.gradle

class XmlCalabashPluginConfiguration implements XmlCalabashPluginOptions {
    protected final Map<String,Object> options = [:]
    protected final Map<String,Object> pluginOptions = [:]

    Map<String,Object> getOptions() {
        return options
    }

    Map<String,Object> getPluginOptions() {
        return pluginOptions
    }

    void setOption(String name, Object value) {
        options[name] = value
    }

    void setPluginOption(String name, Object value) {
        pluginOptions[name] = value
    }
}

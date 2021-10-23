package com.xmlcalabash.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin

abstract class XmlCalabashPluginExtension {
  def config = new XmlCalabashPluginConfiguration()

  void configure(Closure cl) {
    cl.delegate = config
    cl()
  }

  Map<String,Object> getOptions() {
    Map<String,Object> opts = [:]
    config.getOptions().each { key, val ->
      opts[key] = val
    }
    return opts
  }

  Map<String,Object> getPluginOptions() {
    Map<String,Object> opts = [:]
    config.getPluginOptions().each { key, val ->
      opts[key] = val
    }
    return opts
  }
}

class XmlCalabashPlugin implements Plugin<Project> {
  @Override
  void apply(Project project) {
    project.extensions.create('xmlcalabash', XmlCalabashPluginExtension)
    project.task('xproc', type: XmlCalabashTask)
  }
}

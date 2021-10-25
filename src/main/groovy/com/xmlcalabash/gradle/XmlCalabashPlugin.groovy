package com.xmlcalabash.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin

class XmlCalabashPlugin implements Plugin<Project> {
  @Override
  void apply(Project project) {
    project.task('xproc', type: XmlCalabashTask)
  }
}

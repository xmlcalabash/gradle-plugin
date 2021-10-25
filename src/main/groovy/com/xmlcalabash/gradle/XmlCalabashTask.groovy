package com.xmlcalabash.gradle

import org.xml.sax.InputSource
import javax.xml.transform.sax.SAXSource

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.file.FileCollection
import org.gradle.api.InvalidUserDataException

import javax.inject.Inject
import java.nio.file.Path

import net.sf.saxon.s9api.Processor
import net.sf.saxon.s9api.XdmValue

import com.xmlcalabash.XMLCalabash

import com.fasterxml.jackson.core.JsonGenerationException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper

class XmlCalabashTask extends DefaultTask {
  protected final Map<String, ArrayList<XProcValue>> xp_options = [:]
  protected final Map<String, ArrayList<XProcValue>> xp_inputs = [:]
  protected final Map<String, ArrayList<String>> xp_configs = [:]
  protected final Map<String, File> xp_outputs = [:]
  protected Processor saxon = null
  protected XProcValue pipeline = null
  protected boolean debugTask = false
  protected boolean running = false

  // ============================================================

  void debugTask(boolean debug) {
    debugTask = debug
  }

  void processor(Processor processor) {
    saxon = processor
  }

  void input(String port, Object data) {
    this.input(port, data, null)
  }

  void input(String port, Object data, String contentType) {
    def value = new XProcValue(data, contentType)
    if (!(port in xp_inputs)) {
      xp_inputs.put(port, new ArrayList<XProcValue>())
    }
    xp_inputs[port] += value
  }

  void output(String port, Object output) {
    if (!(port in xp_outputs)) {
      xp_outputs[port] = new ArrayList<XProcValue>()
    }
    xp_outputs[port] += project.file(output)
  }

  void pipeline(Object xpl) {
    this.pipeline(xpl, null)
  }

  void pipeline(Object xpl, String contentType) {
    pipeline = new XProcValue(xpl, contentType)
  }

  void option(String eqname, Object value) {
    this.option(eqname, value, null)
  }
  
  void option(String eqname, Object data, String contentType) {
    def value = null

    if (data instanceof String) {
      if (contentType == null) {
        value = new XProcValue(data, "x-raw/string")
      } else {
        value = new XProcValue(data, contentType)
      }
    } else {
      value = new XProcValue(data, contentType)
    }

    if (!(eqname in xp_options)) {
      xp_options.put(eqname, new ArrayList<XProcValue>())
    }

    xp_options[eqname] += value
  }    

  void config(String eqname, String value) {
    if (!(eqname in xp_configs)) {
      xp_configs.put(eqname, new ArrayList<String>())
    }
    xp_configs[eqname] += value
  }    

  void config(String eqname, Boolean value) {
    if (!(eqname in xp_configs)) {
      xp_configs.put(eqname, new ArrayList<String>())
    }
    xp_configs[eqname] += value.toString()
  }    

  // ============================================================

  @OutputFiles
  FileCollection getOutputFiles() {
    FileCollection files = project.files()
    xp_outputs.keySet().each { port ->
      xp_outputs[port].each { value ->
        files.from(value)
      }
    }
    return files
  }

  @InputFiles
  FileCollection getInputFiles() {
    FileCollection files = project.files()
    if (pipeline.fileValue != null) {
      files.from(pipeline.fileValue)
    }
    xp_inputs.keySet().each { port ->
      xp_inputs[port].each { value ->
        if (value.fileValue != null) {
          files.from(value.fileValue)
        }
      }
    }
    return files
  }

  @TaskAction
  void run() {
    running = true
    if (pipeline == null) {
      throw new GradleException("No pipeline provided.")
    }

    XMLCalabash xproc
    if (saxon == null) {
      if (debugTask) {
        println("Creating XMLCalabash with new processor")
      }
      xproc = XMLCalabash.newInstance()
    } else {
      if (debugTask) {
        println("Creating XMLCalabash with supplied processor")
      }
      xproc = XMLCalabash.newInstance(saxon)
    }

    if (debugTask) {
      println("Pipeline: ${pipeline}")
    }

    if (pipeline.contentType != null) {
      xproc.args.pipeline(pipeline.value, pipelien.contentType)
    } else {
      xproc.args.pipeline(pipeline.value)
    }

    xp_inputs.keySet().each { port ->
      xp_inputs[port].each { value ->
        if (debugTask) {
          println("Input ${port}: ${value}")
        }

        if (value.contentType != null) {
          xproc.args.input(port, value.value, value.contentType)
        } else {
          xproc.args.input(port, value.value)
        }
      }
    }

    xp_outputs.keySet().each { port ->
      xp_outputs[port].each { value ->
        if (debugTask) {
          println("Output ${port}: ${value}")
        }

        xproc.args.output(port, value.getAbsolutePath())
      }
    }

    xp_options.keySet().each { eqname ->
      xp_options[eqname].each { value ->
        if (debugTask) {
          println("Option ${eqname}: ${value}")
        }

        if (value.contentType != null) {
          xproc.args.option(eqname, value.value, value.contentType)
        } else {
          xproc.args.option(eqname, value.value)
        }
      }
    }

    xp_configs.keySet().each { eqname ->
      xp_configs[eqname].each { value ->
        if (debugTask) {
          println("Config ${eqname}: ${value}")
        }

        xproc.args.config(eqname, value)
      }
    }

    try {
      xproc.run()
    } catch (Exception e) {
      println(xproc.errorMessage())
      throw e
    }
    running = false
  }

  private class XProcValue {
    Object value = null
    String ctype = null
    File fvalue = null

    Object getValue() {
      return value
    }

    Object getFileValue() {
      return fvalue
    }

    String getContentType() {
      return ctype
    }
    
    XProcValue(Object data) {
      this(data, null)
    }

    XProcValue(Object data, String contentType) {
      if (contentType != null) {
        if (contentType == "xml") {
          ctype = "application/xml"
        } else if (contentType == "json") {
          ctype = "application/json"
        } else if (contentType == "text") {
          ctype = "text/plain"
        } else {
          ctype = contentType
        }
      }

      if (data instanceof File) {
        fvalue = data
        if (contentType == null) {
          // Dunno what type of file it is, pass it in as a URI so
          // that XML Calabash will attempt to sort out its content
          // type.
          value = data.toURI()
        } else {
          value = data
        }
      } else if (data instanceof URI) {
        value = data
      } else if (data instanceof HashMap || data instanceof ArrayList) {
        def mapper = new ObjectMapper()
        value = mapper.writeValueAsString(data)
        if (contentType == null) {
          ctype = "application/json"
        }
      } else if (data instanceof groovy.xml.slurpersupport.GPathResult) {
        value = groovy.xml.XmlUtil.serialize(data)
        if (contentType == null) {
          ctype = "application/xml"
        }
      } else if (data instanceof groovy.util.slurpersupport.GPathResult) {
        value = groovy.util.XmlUtil.serialize(data)
        if (contentType == null) {
          ctype = "application/xml"
        }
      } else if (data instanceof XdmValue) {
        value = data
        ctype = null
      } else if (data instanceof String) {
        if (contentType == null) {
          fvalue = project.file(data)
          value = fvalue.toURI()
        } else {
          value = data
          if (contentType == "x-raw/string") {
            ctype = null
          }
        }
      } else {
        data = value // Let the chips fall where they may
      }
    }

    String toString() {
      if (value instanceof File) {
        return "File ${value.getAbsolutePath}"
      } else if (value instanceof URI) {
        return "URI ${value}"
      } else if (value instanceof String) {
        if (ctype == null) {
          return "String '${value}'"
        } else {
          return "${ctype} '${value}'"
        }
      } else if (value instanceof XdmValue) {
        return "XdmValue ${value}"
      } else {
        return "${value.getClass().getName()} ${value}"
      }
    }
  }
}

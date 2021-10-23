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

import com.xmlcalabash.XMLCalabash

class XmlCalabashTask extends DefaultTask implements XmlCalabashPluginOptions {
  protected final Map<String, Object> options = [:]
  protected final Map<String, Object> pluginOptions = [:]
  protected final ArrayList<XProcInput> inputports = []
  protected final Map<String, File> outputports = [:]
  protected final Map<String, String> pipelineOptions = [:]

  protected File pipeline = null

  // ============================================================

  void setOption(String name, Object value) {
    if (name == 'graph') {
      if (value instanceof ArrayList) {
        options[name] = value
      } else {
        if (!(name in options)) {
          options[name] = new ArrayList()
        }
        options[name] += value
      }
    } else {
      options[name] = value
    }
  }

  void setPluginOption(String name, Object value) {
    pluginOptions[name] = value
  }

  Object getOption(String name) {
    return options[name]
  }

  Object getPluginOption(String name) {
    return pluginOptions[name]
  }

  // ============================================================

  void input(Object input) {
    def iport = null
    def iuri = null
    def ifile = null
    def ctype = null

    if (input instanceof HashMap) {
      input.keySet().each { key ->
        if (key == "port") {
          iport = input.get(key)
        } else if (key == "uri") {
          iuri = input.get(key)
        } else if (key == "file") {
          ifile = input.get(key)
        } else if (key == "content-type") {
          ctype = input.get(key)
        } else {
          throw new GradleException("XML Calabash input map contains invalid key: " + key)
        }
      }
    } else {
      ifile = input
      ctype = "application/xml"
    }
    if (iport == null) {
      iport = "source"
    }

    if (iuri != null && ifile != null) {
      throw new GradleException("XML Calabash input map may contain only one of 'file' and 'uri'")
    }

    if (iuri == null && ifile == null) {
      throw new GradleException("XML Calabash input map must contain one of 'file' or 'uri'")
    }

    if (iuri == null) {
      inputports.add(new XProcInput(iport, project.file(ifile), ctype))
    } else {
      inputports.add(new XProcInput(iport, new URI(iuri)))
    }
  }

  void input(String port, Object input) {
    inputports.add(new XProcInput(port, input, "xml"))
  }

  void output(String port, Object output) {
    if (!(port in outputports)) {
      outputports[port] = new ArrayList()
    }

    outputports[port] += project.file(output)
  }

  void pipeline(Object xpl) {
    // If it's a string that looks like a URI, try to make it a URI
    if (xpl instanceof String && xpl ==~ /^\S+:\/\/.*/) {
      try {
        xpl = new URI(xpl)
      } catch (URISyntaxException ex) {
        // nevermind
      }
    }

    if (xpl instanceof URI) {
      // If it's a file: URI, turn it into a file
      if (xpl.getScheme() == "file") {
        def xplfile = project.file(xpl.getPath())
        pipeline = xplfile
      } else {
        // Otherwise just try to load it (for the output method)
        pipeline = xpl
      }
    } else {
      // If it's not a URI, assume it's a file, load it if it exists
      def xplfile = project.file(xpl)
      pipeline = xplfile
    }
  }

  void option(String eqname, Object value) {
    if (eqname in pipelineOptions) {
      throw new GradleException("Option names cannot be repeated: ${eqname}")
    }
    pipelineOptions[eqname] = value
  }

  @OutputFiles
  FileCollection getOutputFiles() {
    FileCollection files = project.files()
    outputports.keySet().each { key ->
      outputports[key].each { f ->
        files += project.files(f)
      }
    }
    return files
  }

  @InputFiles
  @SkipWhenEmpty
  FileCollection getInputFiles() {
    FileCollection files = project.files()
    files += project.files(pipeline)
    inputports.each { xi ->
      if (xi.getFile() != null) {
        files += project.files(xi.getFile())
      }
    }
    return files
  }

  @TaskAction
  void run() {
    def xproc = XMLCalabash.newInstance()
    xproc.args.pipeline(this.pipeline)

    inputports.each { xi ->
      if (xi.getFile() != null) {
        xproc.args.input(xi.getPort(), xi.getFile(), xi.getContentType())
      } else {
        xproc.args.input(xi.getPort(), xi.getURI())
      }
    }

    outputports.keySet().each { key ->
      outputports[key].each { f ->
        xproc.args.output(key, f.getAbsolutePath())
      }
    }

    options.keySet().each { name ->
      if (options.get(name) instanceof ArrayList) {
        options.get(name).each { value ->
          xproc.args.option(name, value)
        }
      } else {
        xproc.args.option(name, options.get(name))
      }
    }

    Boolean debug = getPluginOption('debug')

    try {
      xproc.run()
    } catch (Exception e) {
      println(xproc.errorMessage())
      throw e
    }
  }

  private class XProcInput {
    private String iport = null
    private File ifile = null
    private URI iuri = null
    private String ctype = null

    String getPort() { return iport }
    File getFile() { return ifile }
    URI getURI() { return iuri }
    String getContentType() {
      if (ctype == "text") {
        return "text/plain"
      } else if (ctype == "html") {
        return "text/html"
      } else if (ctype == "json") {
        return "application/json"
      } else if (ctype == "xml" || ctype == null) {
        return "application/xml"
      } else {
        return ctype
      }
    }

    XProcInput(String port, File infile, String contentType) {
      iport = port
      ifile = infile
      ctype = contentType
    }

    XProcInput(String port, URI uri) {
      iport = port
      iuri = uri
    }
  }
}

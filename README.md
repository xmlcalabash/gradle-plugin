# gradle-plugin

A Gradle plugin for running XML Calabash 3.x tasks.

## Configuration

Add the plugin to your `plugins`:

```
plugins {
  id 'com.xmlcalabash.gradle.xmlcalabash' version '0.0.2'
}
```

Import the task type:

```
import com.xmlcalabash.gradle.XmlCalabashTask
```

Create tasks of that type:

```
task xmlidentity(type: XmlCalabashTask) {
  pipeline 'pipe.xpl'
  input 'source' 'mydoc.xml'
  output 'result', 'output.xml'
}
```

## API

There are six things you can specify: the pipeline, inputs, outputs, options,
configuration settings for XML Calabash process, and the processor.

### The pipeline

The pipeline is specified with `pipeline`:

```
pipeline document [, contentType]
```

The `document` can be a file, a URI, or a string. If it’s a string,
it’ll be interpreted as a filename unless you also specify a content
type. XML Calabash only accepts XML pipelines, so the content type has
to be “`application/xml`”.

### Inputs

Inputs are specified with `input`:

```
input port, data [, contentType]
```

Port must be a string and identifies the input port. Data can be

*  A file. If the content type isn’t specified, the input will be
   passed to XML Calabash as a file URI so that the processor will
   attempt to determine the media type from the URI.
* A URI.
* A Gradle map or array which will be passed to XML Calabash as JSON.
* XML constructed with Groovy’s `XmlSlurper`.
* An XDM item constructed with Saxon. If (and only if) you specify a
  Saxon-created XDM, you must also pass the processor.
* Or a string. If the content type isn’t specified, the string will
  be interpreted as a filename.

### Outputs

Outputs are specified with `output`:

```
output port, file
```

The output `file` may be a string or a file.

### Options

Options are specified with `option`:

```
option name, data [, contentType]
```

The name must be a string. If the option name is in a namespace, use
an EQName to identify it.

The data may be:

*  A file. If the content type isn’t specified, the input will be
   passed to XML Calabash as a file URI so that the processor will
   attempt to determine the media type from the URI.
* A URI.
* A Gradle map or array which will be passed to XML Calabash as JSON.
* XML constructed with Groovy’s `XmlSlurper`.
* An XDM item constructed with Saxon. If (and only if) you specify a
  Saxon-created XDM, you must also pass the processor.
* Or a string.

### Configuration settings

Configuration settings are specified with `config`:

```
config name, value
```

Both name and value must be strings. If the option name is in a
namespace, use an EQName to identify it.

The following standard options are defined:

* `file` The value is a URI that will be loaded as an XML Calabash
  configuration file.
* `stacktrace` The value will be interpreted as a boolean; if true, a
  stack trace will be printed if an error occurs.
* `run` The value will be interpreted as a boolean; if true (or
  unspecified) the pipeline will be run. If false, processing stops
  after the compilation phase.
* `verbose` The value will be interpreted as a boolean; if true, more
  verbose error output will be displayed

### Processor

You can specify a Saxon `Processor` with `processor`:

```
processor saxon
```

The value passed, `saxon`, must be an instance of an Saxon `Processor`
object.

### Task debugging

There’s one more option you can specify, `debugTask`:

```
debugTask value
```

The `value` is a boolean. If it’s true, the gradle task will print a
summary of the inputs, outputs, options, and config settings passed to
the XML Calabash processor.

### A note about content types

When providing a content type, the following shortcuts are supported:

* `xml` means `application/xml`
* `json` means `application/json`
* `text` means `text/plain`

If you don’t specify one of those shortcuts, you must specify the
entire media type.

## Examples

There are several examples under the `examples` directory.

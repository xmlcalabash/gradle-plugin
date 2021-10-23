# gradle-plugin

A Gradle plugin for running XML Calabash 3.x tasks.

Add the plugin to your `plugins`:

```
plugins {
  id 'com.xmlcalabash.gradle.xmlcalabash' version '0.0.1'
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
  input "pipe.xpl"
  output('result', 'output.xml')
}
```

Use a map to specify non-XML documents:

```
task jsonidentity(type: XmlCalabashTask) {
  pipeline 'pipe.xpl'
  input(["file": "doc.json", "content-type": "json"])
  output('result', 'output.json')
  option("test", 5)
}
```

More documentation T.B.D.

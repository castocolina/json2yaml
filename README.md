# json2yaml

Gradle plugin for JSON to YAML tranformation

### Step 1

Apply the plugin

```
plugins {
    id('json2yaml')
}
```

### Step 2

Set input and output file

```
json2yaml.inputFile = project.file('sample.json')
json2yaml.outputFile = project.file('output.yaml')
```

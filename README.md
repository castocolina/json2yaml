# json2yaml

Gradle plugin for JSON to YAML or YAML to JSON tranformation

## Basic Usage

### Install the plugin

Apply the plugin

```
plugins {
    id('json2yaml')
}
```

### Original Tasks (single file conversion)

The original plugin version offered a simple model where a single input and output file could be set for conversion.

For JSON to YAML:
```
json2yaml.inputFile = project.file('sample.json')
json2yaml.outputFile = project.file('output.yaml')
```

For YAML to JSON:
```
yaml2json.inputFile = project.file('sample.yaml')
yaml2json.outputFile = project.file('output.json')
```

### Enhanced Tasks (multiple file conversion)

A newer set of enhanced tasks has been added which allows 

   - multiple files to be converted 
   - adds support for Gradle incremental builds
   - allows either a file collection to be supplied, with conversion outputs flattened, or a file tree which preserves the original directory structure
   - allows file extension mapping to be specified using task configuration options. If the input file extension matches, it  will be swapped with the configured output extension


For JSON to YAML - using file collection:
```
task incremental(type: dev.castocolina.gradle.plugin.JsonToYaml) {
    outDir = file("${buildDir}/generated")
    // collection of files - converted files will appear directly under outDir
    sourceFiles = layout.files('tst.json', 'src/resources/jsonschema/tst2.json')
}
```

For YAML to JSON - using file tree:
```
YamlToJson {
    outDir = file("${buildDir}/generated")
    // file tree - converted files will use same sub-directory structure as originals
    sourceFiles = fileTree(dir: "src", include: "resources/jsonschema/*.yaml")
    // Extension mapping (values shown are actually the defaults). 
    yamlExt = ".yaml"
    jsonExt = ".json"

}
```

/*
 * Various flavours of JSON <> YAML converstion task
 * Note: I am definitely more of a Java coder than a Groovy one, and also on something of a Gradle learning process here
 *       So feel free to critique and suggest improvements!
 */
package dev.castocolina.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.Plugin
import groovy.json.JsonSlurper
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature

import groovy.yaml.YamlSlurper
import com.fasterxml.jackson.core.JsonFactory

import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

public class Json2YamlPluginExtension {
    File inputFile = new File('sample.json')
    File outputFile = new File('build/tmp/docs.yaml')
    Boolean minimizeQuote = Boolean.TRUE
    Boolean deleteTargetFirst = Boolean.TRUE
}

public class Yaml2JsonPluginExtension {
    File inputFile = new File('sample.yaml')
    File outputFile = new File('build/tmp/docs.json')
    Boolean deleteTargetFirst = Boolean.TRUE
}

/**
 * Main class to register plugin tasks
 */
public class Json2YamlPlugin implements Plugin<Project> {
    public void apply(Project project) {

        // Register task from JSON to YAML - from code originally forked
        def extToJson = project.extensions.create('json2yaml', Json2YamlPluginExtension)

        project.tasks.register("json2yaml") {
            def inJson, ouYaml, minQuote, delTarget

            doFirst {
                inJson = extToJson.inputFile
                ouYaml = extToJson.outputFile
                minQuote = extToJson.minimizeQuote
                delTarget = extToJson.deleteTargetFirst

                println "Transform input: " + inJson.absolutePath
                println "Transform output: " + ouYaml.absolutePath
                if (delTarget){
                    println "Delete target first: " + ouYaml.delete()
                }
            }

            doLast {
                JsonToYaml.doIt(inJson, ouYaml, minQuote)
                println("File converted!!!")
            }

        }
        
        // Register task from YAML to JSON - following same approach as code originally forked
        def extToYaml = project.extensions.create('yaml2json', Yaml2JsonPluginExtension)

        project.tasks.register("yaml2json") {
            def inYaml, ouJson, delTarget

            doFirst {
                inYaml = extToYaml.inputFile
                ouJson = extToYaml.outputFile
                delTarget = extToYaml.deleteTargetFirst

                println "Transform input: " + inYaml.absolutePath
                println "Transform output: " + ouJson.absolutePath
                if (delTarget){
                    println "Delete target first: " + ouJson.delete()
                }
            }

            doLast {
                YamlToJson.doIt(inYaml, ouJson)
                println("File converted!!!")
            }

        }
        
        // Alternate approach to register custom tasks for converrsion. This allows use of the 
        // injection of config properties and support for incremental build handling
        project.tasks.register("JsonToYaml", JsonToYaml)        
        project.tasks.register("YamlToJson", YamlToJson)
    }
}

// Newer classes to provide conversion tasks with incremental build support

public abstract class ConverterTask extends DefaultTask {
    @OutputDirectory
    File outDir = project.file("${project.buildDir}/generated")
    
    @InputFiles
    @SkipWhenEmpty
    FileCollection sourceFiles

    @Input
    String yamlExt = ".yaml"    // configurable default extension for YAML files

    @Input
    String jsonExt = ".json"    // configurable default extension for JSON files


    @Internal
    def convertMap = [:]

    void initConversionMap(String extFrom, String extTo) {
        if (sourceFiles instanceof FileTree) {
            // For file trees, we will try and preserve structure on output
            FileTree tree = (FileTree) sourceFiles
            tree.visit {element ->
                if (element.file.isFile()) {
                    convertMap.put(element.file, swapExtension(element.relativePath.toString(), extFrom, extTo))
                }
            }
        } else {
            // For simple collections, output will be flattened
            sourceFiles.each {File file ->
                if (file.isFile()) {
                    convertMap.put(file, swapExtension(file.name, extFrom, extTo))
                }
            }
        } 
    }

    String swapExtension(String path, String extFrom, String extTo) {
        return (path.endsWith(extFrom)) ? path.take(path.lastIndexOf('.')) + extTo
                                        : path + extTo
    }

    void convertAll() {
        convertMap.each{entry -> convert(entry.key, new File(outDir, entry.value))}
    }

    abstract void convert(File fin, File fout)
}

public class YamlToJson extends ConverterTask {

    @TaskAction
    void perform() {
        initConversionMap(yamlExt, jsonExt)
        convertAll()
    }

    void convert(File fin, File fout) {
        doIt(fin, fout)
    }

    static void doIt (File fin, File fout) {
        def yamlText = fin.text
        def yamlMap = new YamlSlurper().parseText(yamlText)
        def factory = new JsonFactory()
        def mapper = new ObjectMapper(factory)

        fout.parentFile.mkdirs()
        mapper.writeValue(fout, yamlMap)
    }
}


public class JsonToYaml extends ConverterTask {

    @Input
    boolean minimizeQuote = true

    @TaskAction
    void perform() {
        initConversionMap(jsonExt, yamlExt)
        convertAll()
    }

    void convert(File fin, File fout) {
        doIt(fin, fout, minimizeQuote)
    }

    static void doIt (File fin, File fout, boolean minQuote) {
        def jsonText = fin.text
        def jSonMap = new JsonSlurper().parseText(jsonText)
        def factory = new YAMLFactory()
        if (minQuote){
             factory.enable(Feature.MINIMIZE_QUOTES)
         }
        def mapper = new ObjectMapper(factory)

        fout.parentFile.mkdirs()
        mapper.writeValue(fout, jSonMap)
    }
}



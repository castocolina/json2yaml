/*
 * This Groovy source file was generated by the Gradle 'init' task.
 */
package dev.castocolina.gradle.plugin

import spock.lang.Specification
import org.gradle.testkit.runner.GradleRunner

/**
 * A simple functional test for the 'json2yaml' plugin.
 */
public class Json2YamlPluginFunctionalTest extends Specification {
    def "can run task"() {
        given:
        def projectDir = new File("build/functionalTest")
        projectDir.mkdirs()
        new File(projectDir, "settings.gradle") << ""
        new File(projectDir, "build.gradle") << """
            plugins {
                id('dev.castocolina.json2yaml')
            }
            json2yaml.inputFile = project.file('../resources/functionalTest/sample.json')
            json2yaml.outputFile = project.file('output.yaml')
        """

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("json2yaml")
        runner.withProjectDir(projectDir)
        def result = runner.build()

        then:
        result.output.contains("File converted!!!")
    }
}

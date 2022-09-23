package me.marvin.scripting.kotlin

import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.dependencies.*
import kotlin.script.experimental.dependencies.maven.MavenDependenciesResolver
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.util.filterByAnnotationType

object ProxyScriptConfigurator : RefineScriptCompilationConfigurationHandler {
    private val resolver = CompoundDependenciesResolver(
        FileSystemDependenciesResolver(),
        MavenDependenciesResolver()
    )

    override operator fun invoke(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
        val diagnostics = arrayListOf<ScriptDiagnostic>()
        val annotations = context.collectedData?.get(ScriptCollectedData.collectedAnnotations)?.takeIf { it.isNotEmpty() }
            ?: return context.compilationConfiguration.asSuccess()

        val scriptBaseDir = (context.script as? FileBasedScriptSource)?.file?.parentFile
        val importedSources = linkedMapOf<String, Pair<File, String>>()

        annotations.filterByAnnotationType<Import>().forEach { scriptAnnotation ->
            scriptAnnotation.annotation.paths.forEach { sourceName ->
                val file = (scriptBaseDir?.resolve(sourceName) ?: File(sourceName)).normalize()
                val keyPath = file.absolutePath
                val prevImport = importedSources.put(keyPath, file to sourceName)

                if (prevImport != null) {
                    diagnostics.add(
                        ScriptDiagnostic(
                            ScriptDiagnostic.unspecifiedError,
                            "Duplicate imports: \"${prevImport.second}\" and \"$sourceName\"",
                            ScriptDiagnostic.Severity.WARNING,
                            context.script.locationId,
                            scriptAnnotation.location?.locationInText
                        )
                    )
                }
            }
        }

        val resolveResult = try {
            runBlocking {
                resolver.resolveFromScriptSourceAnnotations(annotations.filter { it.annotation is DependsOn || it.annotation is Repository })
            }
        } catch (e: Throwable) {
            diagnostics.add(e.asDiagnostics(path = context.script.locationId))
            ResultWithDiagnostics.Failure(diagnostics)
        }

        return resolveResult.onSuccess { resolvedClassPath ->
            ScriptCompilationConfiguration(context.compilationConfiguration) {
                updateClasspath(resolvedClassPath)
                if (importedSources.isNotEmpty()) importScripts.append(importedSources.values.map { FileScriptSource(it.first) })
            }.asSuccess()
        }
    }
}


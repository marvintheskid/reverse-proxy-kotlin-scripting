package me.marvin.scripting.kotlin

import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.providedProperties
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.dependencies.DependsOn
import kotlin.script.experimental.dependencies.Repository
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

object ProxyScriptCompilationConfiguration : ScriptCompilationConfiguration({
    defaultImports(Entrypoint::class, Destructor::class, DependsOn::class, Repository::class, Import::class)
    providedProperties(PROPERTY_TYPES)

    jvm {
        dependenciesFromCurrentContext(wholeClasspath = true)
    }

    refineConfiguration {
        onAnnotations(DependsOn::class, Repository::class, Import::class, handler = ProxyScriptConfigurator)
    }
})
package me.marvin.scripting.kotlin

import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    fileExtension = EXTENSION,
    compilationConfiguration = ProxyScriptCompilationConfiguration::class,
    evaluationConfiguration = ProxyScriptEvaluationConfiguration::class
)
abstract class ProxyScriptDefinition

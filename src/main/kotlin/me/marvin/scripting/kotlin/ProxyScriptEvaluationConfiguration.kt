package me.marvin.scripting.kotlin

import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.providedProperties
import kotlin.script.experimental.api.scriptsInstancesSharing
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm

object ProxyScriptEvaluationConfiguration : ScriptEvaluationConfiguration({
    providedProperties(PROPERTY_VALUES)
    scriptsInstancesSharing(true)

    jvm {
        baseClassLoader(this::class.java.classLoader)
    }
})
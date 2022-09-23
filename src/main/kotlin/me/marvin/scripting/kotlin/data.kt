package me.marvin.scripting.kotlin

import kotlin.reflect.full.memberProperties
import kotlin.script.experimental.api.KotlinType

/**
 * An annotation what can be used in a script to denote a file dependency.
 *
 * @param paths the paths to the dependencies
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FILE)
@Repeatable
annotation class Import(vararg val paths: String)

/**
 * A method annotated with this annotation gets called when the script gets enabled.
 * Methods with this annotation must have 0 parameters.
 *
 * @param priority the priority
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Entrypoint(val priority: Int = 0)

/**
 * A method annotated with this annotation gets called when the script gets disabled.
 * Methods with this annotation must have 0 parameters.
 *
 * @param priority the priority
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Destructor(val priority: Int = 0)

/**
 * The extension of the scripts.
 *
 * @see ProxyScriptDefinition
 * @see ProxyScriptHandler
 */
const val EXTENSION = "proxy.kts"

/**
 * The list of objects to load as properties.
 */
val PROPERTIES = listOf(ProxyScriptConstants)

/**
 * The property values.
 */
val PROPERTY_VALUES = PROPERTIES
    .associateWith { it::class.memberProperties }
    .flatMap { entry ->
        entry.value.map { value ->
            entry.key to (value.name to value.getter.call(entry.key))
        }
    }
    .associate { it.second.first to it.second.second }

/**
 * The property types.
 */
val PROPERTY_TYPES = PROPERTIES
    .map { it::class.memberProperties }
    .flatMap { entry ->
        entry.map { value ->
            value.name to KotlinType(value.returnType)
        }
    }
    .toMap()
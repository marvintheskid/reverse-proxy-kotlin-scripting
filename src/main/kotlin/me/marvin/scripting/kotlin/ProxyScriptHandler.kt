package me.marvin.scripting.kotlin

import org.apache.logging.log4j.Logger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.function.BiConsumer
import java.util.function.Predicate
import java.util.stream.Collectors
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

/**
 * Basic script handler.
 */
class ProxyScriptHandler(private val logger: Logger, parentFolder: Path) {
    companion object {
        private val EXTENSION_PREDICATE: Predicate<Path> = Predicate { file -> file.fileName.toString().endsWith(EXTENSION) }
        private val ENABLED_PREDICATE: Predicate<Path> = Predicate { file -> !file.fileName.toString().startsWith("-") }
        private val COMPILER_CONFIG = createJvmCompilationConfigurationFromTemplate<ProxyScriptDefinition>()
        private val EVALUATION_CONFIG = createJvmEvaluationConfigurationFromTemplate<ProxyScriptDefinition>()
    }

    private val scriptsFolder = parentFolder.resolve("scripts")
    private val scripts: MutableMap<String, ProxyScript>

    /**
     * Loads all scripts on startup.
     */
    init {
        scripts = Files.walk(Files.createDirectories(scriptsFolder)).use { folder ->
            folder
                .filter(EXTENSION_PREDICATE)
                .filter(ENABLED_PREDICATE)
                .peek { file -> logger.info("Loading {}", file.fileName) }
                .map {
                    return@map try {
                        loadScript(it)
                    } catch (e: Exception) {
                        logger.error("An error happened during script loading!", e)
                        null
                    }
                }
                .filter { it != null }
                .map { it!! }
                .peek(ProxyScript::initialize)
                .collect(Collectors.toMap(
                    { it!!.name },
                    { it!! }
                ))
        }
    }

    /**
     * Loops through all the scripts, and executes the given action.
     *
     * @param action the action
     */
    fun forEachScript(action: BiConsumer<Class<out Any>, ProxyScript>) {
        scripts.values.forEach { action.accept(it.instance::class.java, it) }
    }

    /**
     * Disables the given script if possible.
     *
     * @param name the name of the script
     * @param ignoreCheck ignore loaded check
     */
    @JvmOverloads
    fun disableScript(name: String, ignoreCheck: Boolean = false) {
        val enabledPath = scriptsFolder.resolve("$name.$EXTENSION")
        val disabledPath = scriptsFolder.resolve("-$name.$EXTENSION")
        val loaded = scripts.containsKey(name)

        if (!loaded && !ignoreCheck) {
            logger.info("This script ({}) is already disabled!", name)
            return
        }

        if (enabledPath.toFile().exists()) {
            Files.move(enabledPath, disabledPath, StandardCopyOption.ATOMIC_MOVE)
        }

        scripts.remove(name)
            ?.apply { destruct() }
    }

    /**
     * Enables the given script if possible.
     *
     * @param name the name of the script
     * @param ignoreCheck ignore loaded check
     */
    @JvmOverloads
    fun enableScript(name: String, ignoreCheck: Boolean = false) {
        val enabledPath = scriptsFolder.resolve("$name.$EXTENSION")
        val disabledPath = scriptsFolder.resolve("-$name.$EXTENSION")
        val loaded = scripts.containsKey(name)

        if (loaded && !ignoreCheck) {
            logger.info("This script ({}) is already enabled!", name)
            return
        }

        if (disabledPath.toFile().exists()) {
            Files.move(disabledPath, enabledPath, StandardCopyOption.ATOMIC_MOVE)
        }

        loadScript(enabledPath).apply {
            initialize()
            scripts[this.name] = this
        }
    }

    /**
     * Restarts the given script if possible.
     *
     * @param name the name of the script
     */
    fun restartScript(name: String) {
        disableScript(name, true)
        enableScript(name, true)
    }

    private fun loadScript(file: Path): ProxyScript {
        val name = file.fileName.toString()

        val script = BasicJvmScriptingHost().eval(
            file.toFile().toScriptSource(),
            COMPILER_CONFIG,
            EVALUATION_CONFIG
        ).valueOr { result ->
            throw RuntimeException(
                result.reports
                    .filter {
                        it.severity == ScriptDiagnostic.Severity.ERROR
                            || it.severity == ScriptDiagnostic.Severity.FATAL
                            || (it.severity == ScriptDiagnostic.Severity.WARNING
                                && it.code == ScriptDiagnostic.unspecifiedError
                                && it.location != null)
                    }
                    .joinToString("\n") { it.render(withSeverity = false) },
                result.reports.find { it.exception != null }?.exception
            )
        }

        return ProxyScript(
            name.substring(0, name.lastIndexOf(".$EXTENSION")),
            script.returnValue.scriptInstance!!
        )
    }
}
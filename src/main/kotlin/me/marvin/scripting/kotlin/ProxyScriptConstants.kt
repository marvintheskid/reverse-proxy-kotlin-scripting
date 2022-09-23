package me.marvin.scripting.kotlin

import me.marvin.proxy.Proxy
import me.marvin.proxy.commands.impl.CommandTree
import org.apache.logging.log4j.Logger
import java.nio.file.Path

object ProxyScriptConstants {
    lateinit var script: Main
        private set
    lateinit var proxy: Proxy
        private set
    lateinit var logger: Logger
        private set
    lateinit var rootFolder: Path
        private set
    lateinit var commands: CommandTree
        private set

    @JvmStatic
    fun initialize(main: Main, proxy: Proxy, logger: Logger, rootFolder: Path, commands: CommandTree) {
        this.script = main
        this.proxy = proxy
        this.logger = logger
        this.rootFolder = rootFolder
        this.commands = commands
    }
}
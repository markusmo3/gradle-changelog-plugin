package org.jetbrains.changelog.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.ChangelogPluginExtension
import java.io.File

open class PatchChangelogTask : DefaultTask() {

    private val extension = project.extensions.getByType(ChangelogPluginExtension::class.java)

    init {
        group = "build"
    }

    @InputFile
    fun getInputFile() = File(extension.path)

    @OutputFile
    fun getOutputFile() = getInputFile()

    @TaskAction
    fun run() {
        Changelog(extension).apply {
            get(extension.unreleasedTerm).let { item ->
                val header = item.getHeaderNode()
                val arguments = extension.headerArguments.toTypedArray()
                val versionHeader = "## " + extension.headerMessageFormat().format(arguments)

                if (extension.getUnreleased().getSections().isEmpty() && !extension.patchEmpty) {
                    return
                }

                File(extension.path).writeText(content.run {
                    if (extension.keepUnreleasedSection) {
                        val unreleasedGroups = extension.groups.joinToString("\n") { "### $it\n" }
                        println("---")
                        println("\n$unreleasedGroups$versionHeader")
                        println("---")
                        StringBuilder(this).insert(header.endOffset, "\n$unreleasedGroups$versionHeader").toString()
                    } else {
                        replaceRange(header.startOffset, header.endOffset, versionHeader)
                    }
                })
            }
        }
    }
}

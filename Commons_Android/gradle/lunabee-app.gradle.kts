abstract class DownloadStringsTask : DefaultTask() {
    @get:Input
    abstract val locoApiKey: Property<String>

    @get:Input
    abstract val projectDir: Property<String>

    @get:Input
    abstract val commonsDir: Property<String>

    @get:Inject abstract val eo: ExecOperations

    @TaskAction
    fun downloadStrings() {
        val projectDir = projectDir.get()
        val commonsDir = commonsDir.get()
        val locoApiKey = locoApiKey.get()

        val scriptLocation = "$commonsDir/downloadStrings.sh"
        val stringsPath = File("$projectDir/src/main/")
        val stringsFilename = "strings"
        eo.exec {
            commandLine(scriptLocation, locoApiKey, stringsPath, stringsFilename)
        }
    }
}
tasks.register<DownloadStringsTask>("downloadStrings") {
    group = "Lunabee"
    description = "Download all the strings from Locco"

    locoApiKey.set(project.findProperty("LOCO_KEY") as? String ?: "")
    projectDir.set(project.projectDir.path)
    commonsDir.set(project.projectDir.parent + "/Commons_Android")
}

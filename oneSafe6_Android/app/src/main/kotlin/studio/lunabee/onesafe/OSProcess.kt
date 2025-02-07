package studio.lunabee.onesafe

sealed class OSProcess(val main: Boolean, val ime: Boolean, val autofill: Boolean, val commonSetup: Boolean) {
    abstract val pid: Int

    override fun toString(): String {
        return "${this::class.simpleName} #$pid"
    }

    data class Main(override val pid: Int) : OSProcess(main = true, ime = false, autofill = false, commonSetup = true)
    data class Ime(override val pid: Int) : OSProcess(main = false, ime = true, autofill = false, commonSetup = true)
    data class Autofill(override val pid: Int) : OSProcess(main = false, ime = false, autofill = true, commonSetup = true)
    data class Phoenix(override val pid: Int) : OSProcess(main = false, ime = false, autofill = false, commonSetup = false)
    data class DatabaseSetup(override val pid: Int) : OSProcess(main = false, ime = false, autofill = false, commonSetup = false)
    data object Unknown : OSProcess(main = true, ime = true, autofill = true, commonSetup = true) {
        override val pid: Int = -1
    }
}

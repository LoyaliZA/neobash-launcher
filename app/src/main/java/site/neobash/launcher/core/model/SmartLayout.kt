package site.neobash.launcher.core.model

/** Stable storage names; component type and position are independent of rendered pixels. */
enum class BlockSize { COMPACT, MEDIUM, EXPANDED }
data class BlockPreferences(val size: BlockSize = BlockSize.MEDIUM, val position: Int = 0,
    val options: Map<String, String> = emptyMap()) {
    init { require(position >= 0) }
}
data class SmartBlock(val id: String, val component: HomeComponent, val visible: Boolean,
    val supportedSizes: Set<BlockSize>, val preferences: BlockPreferences)

fun Preferences.smartBlocks(): List<SmartBlock> = HomeComponent.entries.mapIndexed { index, component ->
    val id = component.name.lowercase(java.util.Locale.ROOT)
    SmartBlock(id, component, component !in hiddenComponents,
        if (component == HomeComponent.CELESTIAL) BlockSize.entries.toSet() else setOf(BlockSize.MEDIUM),
        blocks[id] ?: BlockPreferences(position = index))
}.sortedWith(compareBy<SmartBlock> { it.preferences.position }.thenBy { it.id })

/** Alias storage is reserved; no alias editor is exposed in this version. */
fun Preferences.favoriteLabel(index: Int, fallback: String): String =
    blocks["favorites"]?.options?.get("alias_$index")?.takeIf { it.isNotBlank() } ?: fallback

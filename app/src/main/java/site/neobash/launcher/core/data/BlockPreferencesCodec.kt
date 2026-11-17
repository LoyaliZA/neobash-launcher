package site.neobash.launcher.core.data

import org.json.JSONObject
import site.neobash.launcher.core.model.*

/** Versioned, additive payload. Unknown option keys survive updates. Legacy keys stay intact. */
internal object BlockPreferencesCodec {
    fun encode(blocks: Map<String, BlockPreferences>): String = JSONObject().apply {
        put("version", 1)
        put("blocks", JSONObject().apply { blocks.forEach { (id, block) ->
            put(id, JSONObject().put("size", block.size.name).put("position", block.position)
                .put("options", JSONObject(block.options)))
        } })
    }.toString()
    fun decode(value: String?): Map<String, BlockPreferences> = try {
        val root = JSONObject(value ?: "{}")
        if (root.optInt("version") != 1) emptyMap() else {
            val blocks = root.optJSONObject("blocks") ?: JSONObject()
            blocks.keys().asSequence().mapNotNull { id ->
                val block = blocks.optJSONObject(id) ?: return@mapNotNull null
                val size = BlockSize.entries.find { it.name == block.optString("size") } ?: BlockSize.MEDIUM
                val options = block.optJSONObject("options") ?: JSONObject()
                id to BlockPreferences(size, block.optInt("position").coerceAtLeast(0),
                    options.keys().asSequence().associateWith { options.optString(it) })
            }.toMap()
        }
    } catch (_: org.json.JSONException) { emptyMap() }
}

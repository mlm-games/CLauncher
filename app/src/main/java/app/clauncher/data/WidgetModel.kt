package app.clauncher.data

import org.json.JSONArray
import org.json.JSONObject

data class WidgetModel(
    val uid: String,
    val appWidgetId: Int,
    val packageName: String,
    val providerClassName: String,
    val heightDp: Int = 120
) {
    fun toJson(): JSONObject = JSONObject()
        .put("uid", uid)
        .put("appWidgetId", appWidgetId)
        .put("packageName", packageName)
        .put("providerClassName", providerClassName)
        .put("heightDp", heightDp)

    companion object {
        fun fromJson(o: JSONObject): WidgetModel? = try {
            WidgetModel(
                uid = o.optString("uid", java.util.UUID.randomUUID().toString()),
                appWidgetId = o.getInt("appWidgetId"),
                packageName = o.getString("packageName"),
                providerClassName = o.getString("providerClassName"),
                heightDp = o.optInt("heightDp", 120)
            )
        } catch (_: Exception) {
            null
        }

        fun listToJson(list: List<WidgetModel>): String {
            val arr = JSONArray()
            list.forEach { arr.put(it.toJson()) }
            return arr.toString()
        }

        fun listFromJson(json: String): MutableList<WidgetModel> {
            val out = mutableListOf<WidgetModel>()
            try {
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    fromJson(arr.getJSONObject(i))?.let { out.add(it) }
                }
            } catch (_: Exception) {
            }
            return out
        }
    }
}

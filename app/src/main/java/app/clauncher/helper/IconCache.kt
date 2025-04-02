package app.clauncher.helper
//
//import android.content.Context
//import android.content.pm.LauncherApps
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import android.graphics.drawable.Drawable
//import android.os.UserHandle
//import androidx.collection.LruCache
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import androidx.core.graphics.createBitmap
//
//class IconCache(private val context: Context) {
//    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
//    private val iconCache = LruCache<String, Bitmap>(100) // Cache 100 icons
//
//    suspend fun getIcon(packageName: String, className: String?, user: UserHandle): Bitmap? {
//        val cacheKey = "$packageName|$className|${user.hashCode()}"
//
//        // Check cache first
//        iconCache.get(cacheKey)?.let { return it }
//
//        // Load icon if not in cache
//        return withContext(Dispatchers.IO) {
//            try {
//                val component = android.content.ComponentName(packageName, className ?: "")
//                val appInfo = launcherApps.resolveActivity(component, user)
//
//                appInfo?.let {
//                    val icon = it.getIcon(0)
//                    val bitmap = drawableToBitmap(icon)
//
//                    // Cache the bitmap
//                    bitmap?.let { iconCache.put(cacheKey, it) }
//                    bitmap
//                }
//            } catch (e: Exception) {
//                null
//            }
//        }
//    }
//
//    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
//        try {
//            val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 48
//            val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 48
//
//            val bitmap = createBitmap(width, height)
//            val canvas = Canvas(bitmap)
//
//            drawable.setBounds(0, 0, canvas.width, canvas.height)
//            drawable.draw(canvas)
//
//            return bitmap
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return null
//        }
//    }
//
//    fun clearCache() {
//        iconCache.evictAll()
//    }
//}
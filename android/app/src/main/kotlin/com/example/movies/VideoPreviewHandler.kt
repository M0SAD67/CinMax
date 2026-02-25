package com.example.movies

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap

class VideoPreviewHandler(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val TAG = "VideoPreviewHandler"
    
    // ✅ Cache variables
    private var currentRetriever: MediaMetadataRetriever? = null
    private var currentPath: String? = null
    private val retrieverMutex = Mutex()
    
    // ⚡ Memory cache للصور
    private val memoryCache = ConcurrentHashMap<Int, ByteArray>()
    private val MAX_CACHE_SIZE = 50 // نحتفظ بـ 50 صورة في الذاكرة

    fun handleMethodCall(
        call: io.flutter.plugin.common.MethodCall,
        result: MethodChannel.Result
    ) {
        when (call.method) {
            "getVideoThumbnail" -> {
                val videoPath = call.argument<String>("videoPath")
                val timeMs = call.argument<Int>("timeMs") ?: 0
                val width = call.argument<Int>("width") ?: 180
                val height = call.argument<Int>("height") ?: 100
                val headers = call.argument<Map<String, String>>("headers")

                if (videoPath == null) {
                    result.error("INVALID_ARGUMENT", "Video path is required", null)
                    return
                }

                getVideoThumbnail(videoPath, timeMs, width, height, headers, result)
            }
            "dispose" -> {
                dispose()
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }

    private fun getVideoThumbnail(
        videoPath: String,
        timeMs: Int,
        width: Int,
        height: Int,
        headers: Map<String, String>?,
        result: MethodChannel.Result
    ) {
        scope.launch {
            try {
                // ⚡ تحقق من الـ Cache أولاً
                val cacheKey = (timeMs / 1000) // نحسب بالثواني بدل الميلي ثانية
                val cachedBytes = memoryCache[cacheKey]
                
                if (cachedBytes != null) {
                    Log.d(TAG, "✅ Cache hit for second: $cacheKey")
                    withContext(Dispatchers.Main) {
                        result.success(cachedBytes)
                    }
                    return@launch
                }
                
                // ✅ مفيش في الـ Cache، نولد صورة جديدة
                val retriever = getRetriever(videoPath, headers)
                
                if (retriever == null) {
                    withContext(Dispatchers.Main) {
                        result.error("RETRIEVER_ERROR", "Failed to initialize retriever", null)
                    }
                    return@launch
                }

                try {
                    // ⚡ الحصول على إطار بأسرع طريقة
                    val bitmap = retriever.getFrameAtTime(
                        timeMs * 1000L,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC // ⚡ أسرع من OPTION_CLOSEST
                    )
                    
                    if (bitmap != null) {
                        processBitmap(bitmap, width, height, cacheKey, result)
                    } else {
                        withContext(Dispatchers.Main) {
                            result.error("NO_FRAME", "No frame available", null)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error getting frame: ${e.message}")
                    withContext(Dispatchers.Main) {
                        result.error("FRAME_ERROR", e.message, null)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ General error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    result.error("EXCEPTION", e.message, e.toString())
                }
            }
        }
    }

    private suspend fun getRetriever(path: String, headers: Map<String, String>?): MediaMetadataRetriever? {
        return retrieverMutex.withLock {
            try {
                // ✅ لو نفس الفيديو، نرجع الـ retriever الموجود
                if (currentRetriever != null && currentPath == path) {
                    return@withLock currentRetriever
                }

                // 🧹 تنظيف القديم
                currentRetriever?.release()
                currentRetriever = null
                memoryCache.clear() // امسح الـ cache لما نغير الفيديو
                
                Log.d(TAG, "🔄 Initializing new MediaMetadataRetriever for: $path")
                val retriever = MediaMetadataRetriever()
                
                if (path.startsWith("http")) {
                    val headersMap = HashMap<String, String>()
                    headers?.forEach { (key, value) ->
                        headersMap[key] = value
                    }
                    retriever.setDataSource(path, headersMap)
                } else {
                    retriever.setDataSource(path)
                }
                
                currentRetriever = retriever
                currentPath = path
                return@withLock retriever
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to initialize retriever: ${e.message}")
                return@withLock null
            }
        }
    }

    private suspend fun processBitmap(
        bitmap: Bitmap,
        width: Int,
        height: Int,
        cacheKey: Int,
        result: MethodChannel.Result
    ) {
        withContext(Dispatchers.IO) {
            try {
                // ⚡ تغيير حجم الصورة بأسرع طريقة
                val scaledBitmap = if (bitmap.width != width || bitmap.height != height) {
                    Bitmap.createScaledBitmap(bitmap, width, height, true) // true = better quality (slower but worth it)
                } else {
                    bitmap
                }
                
                // ⚡ تحويل إلى JPEG مع جودة عالية
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream) // رفعنا الجودة لـ 90
                val bytes = outputStream.toByteArray()
                
                // ✅ حفظ في الـ Memory Cache
                if (memoryCache.size >= MAX_CACHE_SIZE) {
                    // امسح أقدم عنصر
                    val oldestKey = memoryCache.keys.minOrNull()
                    oldestKey?.let { memoryCache.remove(it) }
                }
                memoryCache[cacheKey] = bytes
                
                withContext(Dispatchers.Main) {
                    result.success(bytes)
                }
                
                // 🧹 تنظيف
                if (scaledBitmap != bitmap) {
                    scaledBitmap.recycle()
                }
                bitmap.recycle()
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error processing bitmap: ${e.message}")
                withContext(Dispatchers.Main) {
                    result.error("PROCESS_ERROR", e.message, null)
                }
            }
        }
    }

    fun dispose() {
        scope.launch {
            retrieverMutex.withLock {
                currentRetriever?.release()
                currentRetriever = null
                currentPath = null
                memoryCache.clear()
            }
        }
    }
}
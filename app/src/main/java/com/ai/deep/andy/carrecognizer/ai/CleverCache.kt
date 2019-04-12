package com.ai.deep.andy.carrecognizer.ai

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import com.ai.deep.andy.carrecognizer.utils.Logger
import java.time.LocalDateTime

//tips:
// dynamically scaling cache size based on current memory or battery
// recognize if bitmap is a same car but different angle and pass more image to server

@TargetApi(Build.VERSION_CODES.O)
class CleverCache {

    private class CacheItem(val created: LocalDateTime, val content: Bitmap?, val accuracy: Float)

    private companion object {
        val instance = CleverCache()
    }
    private var cache : MutableMap<LocalDateTime, CacheItem> = HashMap()
    private val maxCacheSize : Int = 20
    private val expiresInSec : Long = 1

    @SuppressLint("NewApi")
    private fun deleteLatestItem(){
        val latest: Map.Entry<LocalDateTime, CacheItem>? = instance.cache.maxWith(Comparator { a, b -> b.value.created.compareTo(a.value.created)})
        instance.cache.remove(latest?.key)
    }

    fun put(item : Bitmap, acc : Float){
        if(instance.cache.size >= maxCacheSize){
            instance.deleteLatestItem()
        }
        val key = LocalDateTime.now()
        val value = CacheItem(key, item, acc)
        instance.cache[key] = value
        Log.d(Logger.LOGTAG, "New bitmap added to cache at $key")
    }


    fun getBest(currentAcc : Float): Bitmap?{
        val now = LocalDateTime.now()
        var best = CacheItem(LocalDateTime.now(), null, currentAcc)
        Log.i(Logger.LOGTAG, "Getting better image from cache than $currentAcc at $now")
        instance.cache.forEach { localDateTime, cacheItem ->
            run {
                if (localDateTime.isAfter(now.minusSeconds(expiresInSec)) && cacheItem.accuracy > best.accuracy) {
                    Log.i(Logger.LOGTAG, "Accuracy increased from ${best.accuracy} into ${cacheItem.accuracy} from $localDateTime")
                    best = cacheItem
                }
            }
        }
        //for debugging
        //printCacheContent()
        return best.content
    }

    private fun printCacheContent(){
        val accuracies : MutableList<String> = ArrayList()
        instance.cache.forEach { _, cacheItem -> accuracies.add(cacheItem.created.toString() + " - " + cacheItem.accuracy)  }
        Log.d(Logger.LOGTAG, "Cache content: $accuracies")
    }

    fun clear(){
        instance.cache.clear()
        Log.i(Logger.LOGTAG, "Cache cleared")
    }
}


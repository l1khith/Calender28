package com.l1khith.calender28.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

private const val TAG = "TaskExportHelper"

object TaskExportHelper {

    /**
     * Saves exported content to the user's public Downloads directory.
     * Works across all Android versions (including Android 10-15+ Scoped Storage).
     */
    fun saveToDownloads(
        context: Context,
        fileName: String,
        content: String,
        mimeType: String
    ): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Calender28")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val collectionUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val itemUri = resolver.insert(collectionUri, contentValues)

                if (itemUri != null) {
                    resolver.openOutputStream(itemUri)?.use { outputStream ->
                        outputStream.write(content.toByteArray(Charsets.UTF_8))
                        outputStream.flush()
                    }

                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(itemUri, contentValues, null, null)
                    Log.d(TAG, "Successfully exported file via MediaStore to: $itemUri")
                    itemUri
                } else {
                    fallbackDirectFileSave(context, fileName, content)
                }
            } else {
                fallbackDirectFileSave(context, fileName, content)
            }
        } catch (e: Exception) {
            Log.e(TAG, "MediaStore export failed, falling back to direct file write", e)
            fallbackDirectFileSave(context, fileName, content)
        }
    }

    private fun fallbackDirectFileSave(
        context: Context,
        fileName: String,
        content: String
    ): Uri? {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val appFolder = File(downloadsDir, "Calender28").apply {
                if (!exists()) mkdirs()
            }
            val targetFile = File(appFolder, fileName)
            FileOutputStream(targetFile).use { fos ->
                fos.write(content.toByteArray(Charsets.UTF_8))
                fos.flush()
            }
            Log.d(TAG, "Saved directly to: ${targetFile.absolutePath}")
            Uri.fromFile(targetFile)
        } catch (e: Exception) {
            Log.e(TAG, "Direct file save failed", e)
            null
        }
    }

    /**
     * Creates an ACTION_SEND Intent to share the exported content.
     */
    fun shareExportedFile(
        context: Context,
        fileName: String,
        content: String,
        mimeType: String
    ) {
        try {
            val cacheFolder = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
            val tempFile = File(cacheFolder, fileName).apply {
                writeText(content, Charsets.UTF_8)
            }

            val fileUri: Uri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )
            } catch (_: Exception) {
                Uri.fromFile(tempFile)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "Calender28 Tasks Export - $fileName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Tasks File").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Log.e(TAG, "Share file failed", e)
        }
    }
}

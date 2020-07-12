package net.banatech.app.android.sabi_alarm.sabi_detector

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.util.Log
import android.util.Log.e
import java.nio.ByteBuffer
import java.util.logging.Logger

object Detector {

    private const val timeOutUs: Long = 1000 // 1ms

    fun detect(soundFileUri: Uri, assets: AssetManager): Array<Int> {
        val sabiArray = arrayOf(0)
        val assetsFile = assets.openFd("default/beethoven_no5_1st.mp3")
        val extractor = MediaExtractor()
        extractor.setDataSource(assetsFile)
        val audioTrackIdx = getAudioTrackIdx(extractor)
        if (audioTrackIdx == -1) {
            Log.e("sabi detector", "audio not found")
            throw RuntimeException("audio not found")
        }
        extractor.selectTrack(audioTrackIdx)
        Log.d("extractor track", extractor.trackCount.toString())
        val audioFormat = extractor.getTrackFormat(audioTrackIdx)
        val audioMime = audioFormat.getString(MediaFormat.KEY_MIME)
        check(audioMime != null) { "Audio file mime must not be null" }
        val decoder = MediaCodec.createDecoderByType(audioMime)
        decoder.configure(audioFormat, null, null, 0)
        decoder.start()
        var isDecodeEnd = false
        var isExtractEnd = false
        Log.d("file length", assetsFile.length.toString())
        val byteArray = ByteArray(assetsFile.length.toInt())
        while(!isDecodeEnd){
            Log.d("while", byteArray.toString())
            if(!isExtractEnd){
                isExtractEnd = extract(extractor, decoder)
            }
            isDecodeEnd = decode(decoder, byteArray)
        }
        decoder.stop()
        decoder.release()
        extractor.release()
        return sabiArray
    }

    private fun getAudioTrackIdx(extractor: MediaExtractor): Int {
        for (idx in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(idx)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio") == true) {
                return idx
            }
        }
        return -1
    }

    private fun extract(extractor: MediaExtractor, decoder: MediaCodec): Boolean {
        var isExtractEnd = false
        val inputBufferIdx = decoder.dequeueInputBuffer(timeOutUs)
        if (inputBufferIdx >= 0) {
            val inputBuffer = decoder.getInputBuffer(inputBufferIdx) as ByteBuffer
            val sampleSize = extractor.readSampleData(inputBuffer, 0)
            if (sampleSize > 0) {
                decoder.queueInputBuffer(
                    inputBufferIdx,
                    0,
                    sampleSize,
                    extractor.sampleTime,
                    extractor.sampleFlags
                )
            } else {
                Log.d("sabi detector", "isExtractEnd = true")
                isExtractEnd = true
                decoder.queueInputBuffer(
                    inputBufferIdx,
                    0,
                    0,
                    0,
                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
            }

            if (!isExtractEnd) {
                extractor.advance()
            }
        }
        return isExtractEnd
    }

    private fun decode(decoder: MediaCodec, bytes: ByteArray): Boolean {
        var isDecodeEnd = false
        val decoderOutputBufferInfo = MediaCodec.BufferInfo()
        val decoderOutputBufferIdx = decoder.dequeueOutputBuffer(decoderOutputBufferInfo, timeOutUs)

        if (decoderOutputBufferInfo.flags != 0 && MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
            Log.d("sabi detector", "isDecodeEnd = true")
            isDecodeEnd = true
        }
        if (decoderOutputBufferIdx >= 0) {
            val decoderOutputBuffer =
                (decoder.getOutputBuffer(decoderOutputBufferIdx) as ByteBuffer).duplicate()
            decoderOutputBuffer.position(decoderOutputBufferInfo.offset)
            decoderOutputBuffer.limit(decoderOutputBufferInfo.offset + decoderOutputBufferInfo.size)

            val flags =
                if (isDecodeEnd) MediaCodec.BUFFER_FLAG_END_OF_STREAM else decoderOutputBufferInfo
            //decoderOutputBuffer.get(bytes)
            //Log.d("bytearray", bytes.toString())
            decoder.releaseOutputBuffer(decoderOutputBufferIdx, false)
            if(decoderOutputBufferInfo.flags != 0 && !isDecodeEnd){
                Log.d("buffer", decoderOutputBuffer[decoderOutputBufferIdx].toString())
            }
        }
        return isDecodeEnd
    }
}
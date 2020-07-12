package net.banatech.app.android.sabi_alarm.sabi_detector

import android.content.res.AssetManager
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

object Detector {

    private const val timeOutUs: Long = 1000 // 1ms
    private var pcmData = arrayListOf<Short>()

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
//        var isExtractEnd = false
//        while(!isExtractEnd){
//            isExtractEnd = extract(extractor, decoder)
//        }
//        val pcmData = getSamplesForChannel(decoder, 0)
//        if(pcmData == null){
//            Log.d("pcm data", "null")
//        }else{
//            pcmData.forEach {
//                Log.d("pcm data", it.toString())
//            }
//        }
        var isDecodeEnd = false
        var isExtractEnd = false
        Log.d("file length", assetsFile.length.toString())
        val byteArray = ByteArray(assetsFile.length.toInt())
        while (!isDecodeEnd) {
            if (!isExtractEnd) {
                isExtractEnd = extract(extractor, decoder)
            }
            isDecodeEnd = decode(decoder, byteArray)
        }
        decoder.stop()
        decoder.release()
        extractor.release()
        Log.d("pcm data size", pcmData.size.toString())
        Log.d("pcm data max", pcmData.max().toString())
        Log.d("pcm data min", pcmData.min().toString())
        pcmData.forEach {
            Log.d("pcm data", it.toString())
        }
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
                Log.d("extract sample time", (extractor.sampleTime/1000).toString())
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
            val buf = decoderOutputBuffer[decoderOutputBufferIdx]
            val pcmSamples = getSamplesForChannel(decoder, decoderOutputBufferIdx, 0)
            if(pcmSamples == null){
                Log.d("pcm samples", "null")
            }else{
                Log.d("pcm size", pcmSamples.size.toString())
                pcmData.add((pcmSamples.sum()/pcmSamples.size).toShort())
//                for(pcmSample in pcmSamples){
//                    pcmData.add(pcmSample)
//                }
            }
            decoder.releaseOutputBuffer(decoderOutputBufferIdx, false)
        }
        return isDecodeEnd
    }

    private fun getSamplesForChannel(codec: MediaCodec, bufferId: Int, channelIx: Int): ShortArray? {
        val outputBuffer = codec.getOutputBuffer(bufferId)
        val format = codec.getOutputFormat(bufferId)
        val samples: ShortBuffer =
            outputBuffer!!.order(ByteOrder.nativeOrder()).asShortBuffer()
        val numChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        if (channelIx < 0 || channelIx >= numChannels) {
            return null
        }
        val res = ShortArray(samples.remaining() / numChannels)
        for (i in res.indices) {
            res[i] = samples.get(i * numChannels + channelIx)
        }
        return res
    }
}
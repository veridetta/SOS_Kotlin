package com.nelayanku.safely.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.text.format.DateFormat
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import com.nelayanku.safely.ui.HomeFragment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class RecorderService : Service() {
    private var mSurfaceView: SurfaceView? = null
    private var mSurfaceHolder: SurfaceHolder? = null
    private var mRecordingStatus = false
    private var mMediaRecorder: MediaRecorder? = null
    private var recordingDuration = 1
    var preview = false
    var picture = false
    override fun onCreate() {
        mRecordingStatus = false
        mServiceCamera = HomeFragment.mCamera
        mSurfaceView = HomeFragment.mSurfaceView
        mSurfaceHolder = HomeFragment.mSurfaceHolder
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        preview = intent.getBooleanExtra("preview", false)
        picture = intent.getBooleanExtra("picture", false)
        if (!mRecordingStatus) startRecording()
        return START_STICKY
    }

    override fun onDestroy() {
        stopRecording()
        mRecordingStatus = false
        super.onDestroy()
    }

    @SuppressLint("NewApi")
    private fun startRecording(): Boolean {
        try {
            Toast.makeText(baseContext, "Recording Started", Toast.LENGTH_SHORT).show()
            Log.d("RecorderService", "startRecording")
            //ambil camera mode dari sharedpreferences
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val mode = sharedPreferences.getString("mode", "Depan")
            if(mode=="Depan"){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    mServiceCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
                    //mengatur kamera agar potrait
                    mServiceCamera!!.setDisplayOrientation(90)
                }
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    mServiceCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
                    //mengatur kamera agar potrait
                    mServiceCamera!!.setDisplayOrientation(90)
                }
            }
            val params = mServiceCamera!!.parameters
            mServiceCamera!!.parameters = params
            val p = mServiceCamera!!.parameters
            val listPreviewSize = p.supportedPreviewSizes
            for (size: Camera.Size in listPreviewSize) {
                Log.i(
                    TAG,
                    String.format("Supported Preview Size (%d, %d)", size.width, size.height)
                )
            }
            val previewSize = listPreviewSize[0]
            p.setPreviewSize(previewSize.width, previewSize.height)
            mServiceCamera!!.parameters = p
            try {
                mServiceCamera!!.setPreviewDisplay(mSurfaceHolder)
                mServiceCamera!!.startPreview()
            } catch (e: IOException) {
                Log.e(TAG, e.message!!)
                e.printStackTrace()
            }
            if (preview) {

            }
            else if(picture) {
                // Membuat nama file gambar berdasarkan timestamp
                val timeStamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())
                val photoDirectory = File(getExternalFilesDir("sos-app"), "media")
                // Membuat direktori jika belum ada
                if (!photoDirectory.exists()) {
                    photoDirectory.mkdirs()
                }
                // Membuat file gambar
                val photoFile = File(
                    photoDirectory.toString() + "/" +
                            "IMG_" + timeStamp + ".jpg"
                )
                // Menyimpan gambar ke file
                mServiceCamera!!.takePicture(null, null, Camera.PictureCallback { data, _ ->
                    try {
                        val fos = photoFile.outputStream()
                        fos.write(data)
                        fos.close()
                        // Menambahkan gambar ke galeri
                        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                        mediaScanIntent.data = Uri.fromFile(photoFile)
                        sendBroadcast(mediaScanIntent)
                        Toast.makeText(
                            baseContext,
                            "Picture saved to $photoFile",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: IOException) {
                        Log.d(TAG, "Error accessing file: $e")
                        e.printStackTrace()
                    }
                })
                mRecordingStatus = false
            }else{
                mServiceCamera!!.unlock()
                mMediaRecorder = MediaRecorder()
                mMediaRecorder!!.setCamera(mServiceCamera)
                mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)
                mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                mMediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                val photoDirectory = File(getExternalFilesDir("sos-app"), "media")
                // Membuat direktori jika belum ada
                if (!photoDirectory.exists()) {
                    photoDirectory.mkdirs()
                }
                mMediaRecorder!!.setOutputFile(
                    photoDirectory.toString() + "/" +
                            DateFormat.format("yyyy-MM-dd_kk-mm-ss", Date().time) +
                            ".mp4"
                )
                mMediaRecorder!!.setPreviewDisplay(mSurfaceHolder!!.surface)
                mMediaRecorder!!.prepare()
                mMediaRecorder!!.start()
                mRecordingStatus = true
                // Schedule the stopRecording() method to be called after the specified duration
                val handler = Handler()
                handler.postDelayed({
                    resetRecording()
                    startRecording()
                    mRecordingStatus = false
                }, (recordingDuration * 1000 * 15 * 60).toLong()) // Convert duration to milliseconds
            }
            return true
        } catch (e: IllegalStateException) {
            Log.d(TAG, (e.message)!!)
            e.printStackTrace()
            return false
        } catch (e: IOException) {
            Log.d(TAG, (e.message)!!)
            e.printStackTrace()
            return false
        }
    }
    //reset recording
    private fun resetRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder!!.reset()
        }
    }
    private fun stopRecording() {
        Toast.makeText(baseContext, "Recording Stopped", Toast.LENGTH_SHORT).show()
        Log.d("RecorderService", "stopRecording")
        try {
            mServiceCamera!!.reconnect()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mMediaRecorder!!.stop()
        mMediaRecorder!!.reset()
        mServiceCamera!!.stopPreview()
        mMediaRecorder!!.release()
        mServiceCamera!!.release()
        mServiceCamera = null
    }

    companion object {
        private val TAG = "RecorderService"
        private var mServiceCamera: Camera? = null
    }
}
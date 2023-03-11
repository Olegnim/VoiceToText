package ru.sokolovsoftware

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*


class MainActivity : AppCompatActivity(), RecognitionListener {

    lateinit var micImage: ImageView
    private lateinit var textOutput: TextView
    lateinit var comment: TextView
    private val REQUEST_CODE_SPEECH_INPUT = 1
    lateinit var speechRecognizer: SpeechRecognizer
    var recordStatus = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        micImage = findViewById(R.id.image_mic)
        textOutput = findViewById(R.id.output_text)
        comment = findViewById(R.id.comment)
        comment.text = "Нажмите на микрофон что бы начать запись"

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkPermission()
        }
        textOutput.setOnClickListener {
            val clipboard: ClipboardManager =
                this.baseContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("", textOutput.text.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                this@MainActivity, "Текст скопирован в буфер обмена",
                Toast.LENGTH_SHORT
            ).show()
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(this)
        micImage.setOnClickListener {
            if (recordStatus) {
                stopRecord()
            } else {
                startRecord()
            }
        }
    }

    override fun onReadyForSpeech(p0: Bundle?) {
        Log.e("RecognitionListener", "onReadyForSpeech $p0")
    }

    override fun onBeginningOfSpeech() {
        Log.e("RecognitionListener", "onBeginningOfSpeech")
    }

    override fun onRmsChanged(p0: Float) {
        Log.e("RecognitionListener", "onRmsChanged $p0")
    }

    override fun onBufferReceived(p0: ByteArray?) {
        Log.e("RecognitionListener", "onBufferReceived $p0")
    }

    override fun onEndOfSpeech() {
        Log.e("RecognitionListener", "onEndOfSpeech")
    }

    override fun onError(p0: Int) {
        speechRecognizer.cancel()
        if (p0 == 7) {
            comment.text = "Не могу распознать речь"
        }
        Log.e("RecognitionListener", "onError $p0")
    }

    override fun onResults(p0: Bundle?) {
        Log.e("RecognitionListener", "onResults $p0")
        stopRecord()
        micImage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_mic_none_24))
        try {
            val data: ArrayList<String>? =
                p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            textOutput.text = data?.get(0)
        } catch (e: Exception) {
            Toast.makeText(
                this@MainActivity, " " + e.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onPartialResults(p0: Bundle?) {
        Log.e("RecognitionListener", "onPartialResults $p0")
    }

    override fun onEvent(p0: Int, p1: Bundle?) {
        Log.e("RecognitionListener", "onEvent $p0")
    }

    override fun isDestroyed(): Boolean {
        speechRecognizer.destroy()
        return super.isDestroyed()
    }

    private fun checkPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_CODE_SPEECH_INPUT
        )
    }

    private fun stopRecord() {
        comment.text = "Нажмите на микрофон что бы начать запись"
        micImage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_mic_none_24))
        recordStatus = false
        speechRecognizer.stopListening()
    }

    private fun startRecord() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.US
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
            30000
        )
        speechRecognizer.startListening(intent)
        comment.text = "Идет запись..."
        micImage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_mic_24))
        recordStatus = true
    }
}

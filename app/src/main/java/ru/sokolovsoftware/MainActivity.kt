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
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale


class MainActivity : AppCompatActivity(), RecognitionListener {

    private lateinit var micImage: ImageView
    private lateinit var textOutput: TextView
    private lateinit var comment: TextView
    private lateinit var clearButton: Button
    private val REQUEST_CODE_SPEECH_INPUT = 1
    private lateinit var speechRecognizer: SpeechRecognizer
    private var recordStatus = false
    private var manualStopRecord = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewInit()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(this)
    }

    private fun viewInit() {
        micImage = findViewById(R.id.image_mic)
        textOutput = findViewById(R.id.output_text)
        comment = findViewById(R.id.comment)
        comment.text = this.getString(R.string.touch_to_start)
        clearButton = findViewById(R.id.clear_button)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkPermission()
        }
        textOutput.gravity = Gravity.LEFT
        textOutput.setOnClickListener {
            val clipboard: ClipboardManager =
                this.baseContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("", textOutput.text.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                this@MainActivity, this.getString(R.string.text_copy_to_clipboard),
                Toast.LENGTH_SHORT
            ).show()
        }
        micImage.setOnClickListener {
            if (recordStatus) {
                manualStopRecord = true
                stopRecord()
            } else {
                manualStopRecord = false
                startRecord()
            }
        }
        clearButton.setOnClickListener { textOutput.text = "" }
    }

    override fun onReadyForSpeech(p0: Bundle?) {
        Log.e("RecognitionListener", "onReadyForSpeech $p0")
    }

    override fun onBeginningOfSpeech() {
        Log.e("RecognitionListener", "onBeginningOfSpeech")
    }

    override fun onRmsChanged(p0: Float) {
    }

    override fun onBufferReceived(p0: ByteArray?) {
        Log.e("RecognitionListener", "onBufferReceived $p0")
    }

    override fun onEndOfSpeech() {
        Log.e("RecognitionListener", "onEndOfSpeech")
    }

    override fun onError(p0: Int) {
        speechRecognizer.cancel()
        if (p0 == 7 || (p0 == 5 && !manualStopRecord)) {
            comment.text = this.getString(R.string.error_7)
            startRecord()
        } else {
            Toast.makeText(
                this@MainActivity, this.getString(R.string.error_x, p0),
                Toast.LENGTH_SHORT
            ).show()
        }
        Log.e("RecognitionListener", "onError $p0")
    }

    override fun onResults(p0: Bundle?) {
        Log.e("RecognitionListener", "onResults $p0")
        stopRecord()
        micImage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_mic_none_24))

        val data: ArrayList<String>? =
            p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        var allOutputText: String = textOutput.text.toString()
        if (allOutputText == this.getString(R.string.text_will_be_here)) {
            allOutputText = ""
        }
        allOutputText += " "
        allOutputText += data?.get(0)
        textOutput.text = allOutputText
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
        comment.text = this.getString(R.string.touch_to_start)
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
        comment.text = this.getString(R.string.recording_in_progress)
        micImage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_mic_24))
        recordStatus = true
    }
}

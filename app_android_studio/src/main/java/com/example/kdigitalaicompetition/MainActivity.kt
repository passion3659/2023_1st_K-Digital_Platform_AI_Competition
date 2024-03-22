package com.example.kdigitalaicompetition

import android.app.Activity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kdigitalaicompetition.databinding.ActivityMainBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    var REQUEST_CAMERA = 0
    private var imageBitmap: Bitmap? = null
    private var question = ""
    lateinit var textToSpeech: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 권한 설정
        requestPermission()
        // RecognizerIntent 생성
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)    // 여분의 키
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")         // 언어 설정


        textToSpeech = TextToSpeech(this, this)

        binding.apply {
            questionEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    // 텍스트가 변경될 때마다 question 변수에 할당
                    question = s.toString()
                }
            })

            // 사진 촬영
            imageView.setOnClickListener {
                Log.d("camera", "이미지 클릭 시 :$imageBitmap")
                TakePicture()
            }

            // 음성 인식
            recordView.setOnClickListener {
                // 새 SpeechRecognizer 를 만드는 팩토리 메서드
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this@MainActivity)
                speechRecognizer.setRecognitionListener(recognitionListener)    // 리스너 설정
                speechRecognizer.startListening(intent)                         // 듣기 시작
            }

            // 서버로 질문, 이미지 전송
            getTextAndSpeechView.setOnClickListener {
                // 질문이나 사진 없으면 toast
                if (questionEditText.text.toString().isEmpty() || imageBitmap == null) {
                    Toast.makeText(this@MainActivity, "사진과 질문을 입력해주세요", Toast.LENGTH_SHORT).show()
                } else {
                    UploadTask().execute(question, imageBitmap!!)
                    Log.d("FlaskResponse", "$question")
                }
            }
        }
    }

    // 카메라 앱으로 이동
    private fun TakePicture() {
        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)    //이미지 캡처 액션 가진 intent 생성
        startActivityForResult(intent, REQUEST_CAMERA)          //생성한 intent로 카메라 앱 실행
    }

    // 사진 촬영 결과 처리
    //REQUEST_CAMERA 요청 코드와 Activity.RESULT_OK 결과 코드 확인하여 촬영이 성공한 경우에 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            imageBitmap = data?.extras?.get("data") as Bitmap   //촬영한 이미지를 Bitmap형태로 가져옴
            Log.d("camera", "onResult : $imageBitmap")
        }
        if (imageBitmap == null) {
            Log.d("camera", "Toast 부분 : $imageBitmap")
            Toast.makeText(this@MainActivity, "사진을 불러오지 못했습니다", Toast.LENGTH_SHORT).show()
        } else {
            binding.imageView.setImageBitmap(imageBitmap)
        }
    }

    private inner class UploadTask : AsyncTask<Any, Void, String>() {
        override fun doInBackground(vararg params: Any?): String {
            try {
                // Flask 서버 엔드포인트 URL
                val serverUrl = "http://10.101.134.151:5050/"
                Log.d("FlaskResponse", "$serverUrl")

                // 질문 및 이미지 데이터
                val question = params[0] as String
                val imageBitmap = params[1] as Bitmap
                Log.d("FlaskResponse", "$question")
                Log.d("FlaskResponse", "$imageBitmap")

                // Bitmap을 File로 변환
                val imageFile = convertBitmapToFile(imageBitmap)

                // Retrofit 인스턴스 생성
                val retrofit = Retrofit.Builder()
                    .baseUrl(serverUrl)
                    .client(OkHttpClient.Builder().build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                // 서비스 인터페이스 생성
                val apiService = retrofit.create(PredictService::class.java)

                // 질문에 대한 RequestBody 생성
                val questionBody = question.toRequestBody("text/plain".toMediaTypeOrNull())

                // 이미지 파일에 대한 RequestBody 생성
                val imageRequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart =
                    MultipartBody.Part.createFormData("imageFile", imageFile.name, imageRequestBody)

                // 요청 수행
                val call = apiService.uploadImage(questionBody, imagePart)
                val response = call.execute()

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    return responseBody?.result ?: "빈 응답"
                } else {
                    return "에러: ${response.code()}"
                }

            } catch (e: Exception) {
                Log.e("FlaskResponse", "에러: ${e.message}")
                return "에러: ${e.message}"
            }
        }

        override fun onPostExecute(result: String) {
            // 서버 응답 처리
            Log.d("FlaskResponse", result)
            try {
                binding.answerTextView.setText(result)
                onInit(TextToSpeech.SUCCESS)
                Log.d("FlaskResponse", "이미지 경로 성공적으로 받음")
            } catch (e: JSONException) {
                Log.e("FlaskResponse", "JSON 파싱 에러: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // 이미지 비트맵을 파일로 변환하는 함수
    private fun convertBitmapToFile(bitmap: Bitmap): File {
        val file = File(cacheDir, "image.jpg")
        file.createNewFile()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, file.outputStream())
        return file
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val languageStatus: Int = textToSpeech.setLanguage((Locale.KOREAN))

            if (languageStatus == TextToSpeech.LANG_MISSING_DATA ||
                languageStatus == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show()
            } else {
                val data: String = binding.answerTextView.text.toString()
                var speechStatus: Int = 0

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    speechStatus = textToSpeech.speak(data, TextToSpeech.QUEUE_FLUSH, null, null)
                } else {
                    speechStatus = textToSpeech.speak(data, TextToSpeech.QUEUE_FLUSH, null)
                }
                if (speechStatus == TextToSpeech.ERROR) {
                    Toast.makeText(this, "음성전환 에러", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "음성전환 에러", Toast.LENGTH_SHORT).show()
        }
    }
    // 리스너 설정
    private val recognitionListener: RecognitionListener = object : RecognitionListener {
        // 말하기 시작할 준비가되면 호출
        override fun onReadyForSpeech(params: Bundle) {
            binding.tvState.text = "이제 말씀하세요!"
            val startDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.baseline_record_voice_over_24)
            binding.tvState.setCompoundDrawablesWithIntrinsicBounds(startDrawable, null, null, null)
        }
        // 말하기 시작했을 때 호출
        override fun onBeginningOfSpeech() {
            binding.tvState.text = "잘 듣고 있어요."
            val loadingDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.baseline_hearing_24)
            binding.tvState.setCompoundDrawablesWithIntrinsicBounds(loadingDrawable, null, null, null)
        }
        // 입력받는 소리의 크기를 알려줌
        override fun onRmsChanged(rmsdB: Float) {
            Log.d("speech", "sound : $rmsdB")
        }
        // 말을 시작하고 인식이 된 단어를 buffer에 담음
        override fun onBufferReceived(buffer: ByteArray) {
        }
        // 말하기를 중지하면 호출
        override fun onEndOfSpeech() {
            binding.tvState.text = "Success!"
            val successDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.baseline_check_24)
            binding.tvState.setCompoundDrawablesWithIntrinsicBounds(successDrawable, null, null, null)
        }
        // 오류 발생했을 때 호출
        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
                SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "퍼미션 없음"
                SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트웍 타임아웃"
                SpeechRecognizer.ERROR_NO_MATCH -> "찾을 수 없음"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER 가 바쁨"
                SpeechRecognizer.ERROR_SERVER -> "서버가 이상함"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "말하는 시간초과"
                else -> "알 수 없는 오류임"
            }
            val errorDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.baseline_error_24)
            binding.tvState.setCompoundDrawablesWithIntrinsicBounds(errorDrawable, null, null, null)
            binding.tvState.text = "에러 발생: $message"
            binding.questionEditText.setText("다시 한 번 말씀해주세요")
        }
        // 인식 결과가 준비되면 호출
        override fun onResults(results: Bundle) {
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줌
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

            if (matches != null && matches.isNotEmpty()) {
                val recognizedText = matches[0] // 첫 번째 결과를 선택하거나 모든 결과를 순회할 수 있습니다
                binding.questionEditText.setText(recognizedText)
            }
            Log.d("speech", "text-matches : $matches")
        }
        // 부분 인식 결과를 사용할 수 있을 때 호출
        override fun onPartialResults(partialResults: Bundle) {}
        // 향후 이벤트를 추가하기 위해 예약
        override fun onEvent(eventType: Int, params: Bundle) {}
    }

    // 권한 설정 메소드
    private fun requestPermission() {
        // 버전 체크, 권한 허용했는지 체크
        if (Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        }
    }

    private val activityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // 보이스 있으면
        if (it.resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            // 음성 전환 준비
            textToSpeech = TextToSpeech(this, this)
        } else {
            val installIntent: Intent = Intent()
            installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
            startActivity(installIntent)
        }
    }
}
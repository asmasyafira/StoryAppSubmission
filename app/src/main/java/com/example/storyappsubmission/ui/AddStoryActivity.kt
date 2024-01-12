package com.example.storyappsubmission.ui

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.storyappsubmission.R
import com.example.storyappsubmission.data.SsPreferences
import com.example.storyappsubmission.databinding.ActivityAddStoryBinding
import com.example.storyappsubmission.viewmodel.AddStoryViewModel
import com.example.storyappsubmission.viewmodel.UserViewModel
import com.example.storyappsubmission.viewmodel.ViewModelFactory
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var token: String
    private lateinit var fileFinal: File
    private var getFile: File? = null
    private val addStoryViewModel: AddStoryViewModel by lazy {
        ViewModelProvider(this)[AddStoryViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onClicked()

        val pref = SsPreferences.getInstance(dataStore)
        val userViewModel = ViewModelProvider(this, ViewModelFactory(pref))[UserViewModel::class.java]

        userViewModel.getToken().observe(this){
            token = it
        }

        addStoryViewModel.message.observe(this){
            showToast(it)
        }

        addStoryViewModel.loading.observe(this){
            onLoading(it)
        }

        supportActionBar?.title = "Add New Story"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun onClicked() {
        binding.btnPostStory.setOnClickListener {
            if (getFile==null){
                showToast(resources.getString(R.string.uploadWarning))
                return@setOnClickListener
            }

            val des = binding.txtDesc.text.toString()
            if (des.isEmpty()) {
                binding.txtDesc.error = resources.getString(R.string.uploadDescWarning)
                return@setOnClickListener
            }

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val file = getFile as File
                    var compressedFile: File? = null
                    var compressedFileSize = file.length()
                    while (compressedFileSize > 1 * 1024 * 1024) {
                        compressedFile = withContext(Dispatchers.Default) {
                            Compressor.compress(applicationContext, file)
                        }
                        compressedFileSize = compressedFile.length()
                    }

                    fileFinal = compressedFile ?: file
                }

                val requestImageFile = fileFinal.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imgMultiPart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo", fileFinal.name, requestImageFile
                )

                val desPart = des.toRequestBody("text/plain".toMediaType())
                addStoryViewModel.upload(imgMultiPart, desPart, token)
            }
        }

        binding.btnCam.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.resolveActivity(packageManager)
            createCustomFile(application).also {
                val photoURI: Uri = FileProvider.getUriForFile(this@AddStoryActivity, getString(R.string.package_name), it)
                currentPathPhoto = it.absolutePath
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                launcherIntentCamera.launch(intent)
            }
        }

        binding.btnGallery.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            val chooser = Intent.createChooser(intent, "Pilih Gambar")
            launcherIntentGallery.launch(chooser)
        }
    }

    private var anyPhoto = false
    private lateinit var currentPathPhoto: String
    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPathPhoto)
            getFile = myFile
            val result = BitmapFactory.decodeFile(myFile.path)
            anyPhoto = true
            binding.imgUpload.setImageBitmap(result)
            binding.txtDesc.requestFocus()
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectImg, this@AddStoryActivity)
            getFile = myFile
            binding.imgUpload.setImageURI(selectImg)
            binding.txtDesc.requestFocus()
        }
    }

    private fun uriToFile(selectImg: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = createCustomFile(context)
        val inputStream = contentResolver.openInputStream(selectImg) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buffer = ByteArray(1024)
        var len: Int
        while (inputStream.read(buffer).also { len = it } > 0) outputStream.write(buffer, 0, len)
        outputStream.close()
        inputStream.close()

        return myFile
    }

    private val timeStamp: String = SimpleDateFormat(NAME_FILE, Locale.US).format(System.currentTimeMillis())

    private fun createCustomFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this@AddStoryActivity, StringBuilder(getString(R.string.message)).append(msg), Toast.LENGTH_SHORT).show()
        val intent = Intent(this@AddStoryActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun onLoading(it: Boolean) {
        binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
    }

    companion object {
        const val NAME_FILE = "MMddyyyy"
    }
}
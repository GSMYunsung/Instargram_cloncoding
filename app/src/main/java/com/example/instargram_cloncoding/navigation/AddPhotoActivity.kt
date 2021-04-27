package com.example.instargram_cloncoding.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.instargram_cloncoding.R
import com.example.instargram_cloncoding.databinding.ActivityAddPhotoBinding
import com.example.instargram_cloncoding.databinding.ActivityMainBinding
import com.google.android.gms.common.internal.Objects
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*


//만약 오류가 생긴다면 파이어베이스 스토리지 버전을 업그레이드 해야한다.

class AddPhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPhotoBinding
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_photo)
        //Initiate storge
        storage = FirebaseStorage.getInstance()

        //엑티비티를 실행하자마자 카메라 엘범 오픈하게 해주는 코드
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        //버튼에 이벤트 넣어주기
        binding.addphotoBtnUpload.setOnClickListener {
            contentUpload()
        }
    }

    //선택한 이미지를 받는 부분
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == Activity.RESULT_OK) {
                //사진을 선택했을때 사진의 경로 넘어오는 부분
                photoUri = data?.data
                Log.d("uri",photoUri.toString())
                //이미지뷰에다가 자신이 선택한 사진을 보여준다.
                binding.addphotoImage.setImageURI(photoUri)
            } else {
                //취소버튼을 눌렀을때 즉, 사진의 경로가 전송되지 않고 취소되었을때 부분
                //그냥 엑티비티를 닫는다.
                finish()
            }
        }
    }

    fun contentUpload() {
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"

        //스토리지 레퍼런스에 사진을 업로드한다.
        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        //샤진 업로드
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_LONG).show()
        }
    }
}

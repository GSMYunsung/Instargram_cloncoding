package com.example.instargram_cloncoding

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.instargram_cloncoding.databinding.ActivityMainBinding
import com.example.instargram_cloncoding.navigation.*
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding : ActivityMainBinding

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        setToolbarDefault()
        when(item.itemId){
            R.id.action_home ->{
                var detailViewFragment = DetailViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,detailViewFragment).commit()
                return true
            }
            R.id.action_account ->{
                var userframgnet = UserFragment()
                var bundle = Bundle()
                //유저 정보 받아오기
                var uid = FirebaseAuth.getInstance().currentUser.uid
                bundle.putString("destinationUid",uid)
                userframgnet.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.main_content,userframgnet).commit()
                return true
            }
            R.id.action_add_photo ->{
                //사진 경로를 가져올 수 있는지 없는지 판별
                if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                startActivity(Intent(this,AddPhotoActivity::class.java))
                    return true
            }
            R.id.action_favorite_alarm ->{
                var alarmfragment = AlramFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,alarmfragment).commit()
                return true
            }
            R.id.action_search ->{
                var gridFragment = GridFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,gridFragment).commit()
                return true
            }
        }
        return false
    }
    fun setToolbarDefault(){
        findViewById<TextView>(R.id.toolbar_user_name).visibility = View.GONE
        findViewById<ImageView>(R.id.toolbar_btn_back).visibility = View.GONE
        findViewById<ImageView>(R.id.toolbar_title_image).visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)
        //사진의 경로를 가져와 줄 수 있는 코드
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
        
        //메인화면이 뜨면 메인화면 프레그먼트가 메인화면으로 뜨게하는 코드
        binding.bottomNavigation.selectedItemId = R.id.action_home
    }


    //프로필 사진 넣는곳
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == UserFragment.PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK){
            var imagUir = data?.data
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            //userProfileImages라는 폴더를 생성, 파일이름을 uid로 넣어줌
            var storegeRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)
            //이미지를 다운로드 해주기위해!
            storegeRef.putFile(imagUir!!).continueWithTask { task : Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storegeRef.downloadUrl
            }.addOnSuccessListener { uri ->
                var map = HashMap<String,Any>()
                map["image"] = uri.toString()
                FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map)
            }
        }
    }

}

package com.example.instargram_cloncoding

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.instargram_cloncoding.databinding.ActivityMainBinding
import com.example.instargram_cloncoding.navigation.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding : ActivityMainBinding

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_home ->{
                var detailViewFragment = DetailViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,detailViewFragment).commit()
                return true
            }
            R.id.action_account ->{
                var gridFragment = GridFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,gridFragment).commit()
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
                var userframgnet = UserFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,userframgnet).commit()
                return true
            }
        }
        return false
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

}

package com.example.instargram_cloncoding.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.instargram_cloncoding.R
import com.example.instargram_cloncoding.navigation.DataModle.ContentDTOs
import com.example.instargram_cloncoding.navigation.DetailViewFragment.DetailViewRecyclerViewAdapter.CustomViewHolder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserFragment : Fragment() {

    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var auth : FirebaseAuth? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false) //프레그먼트 레이아웃 받아오
        return fragmentView

    }

    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var contentDTO : ArrayList<ContentDTOs> = arrayListOf()
        init {
            firestore?.collection("images")?.whereEqualTo("uid", uid)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot == null) return@addSnapshotListener

                //Data 받아오기
                for (snapshot in querySnapshot.documents) {
                    contentDTO.add(snapshot.toObject(ContentDTOs::class.java)!!)
                }
                view?.findViewById<TextView>(R.id.account_tv_post_count)?.text = contentDTO.size.toString()
                //리사이클러뷰 새로고침
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            // 화면 비율의 1/3인 정사각형이 만들어짐
            var width = resources.displayMetrics.widthPixels / 3

            var imageview = ImageView(parent.context)
            //레이아웃 파라미터 즉, 리니어 레이아웃의 이미지뷰를 불러온다음 크기를 화면비율의 1/3 정사각형으로 만든다.
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width,width)
            return CustomViewHolder(imageview)
        }

        inner class CustomViewHolder(imageView: ImageView) : RecyclerView.ViewHolder(){

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            TODO("Not yet implemented")
        }

        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }

    }
}
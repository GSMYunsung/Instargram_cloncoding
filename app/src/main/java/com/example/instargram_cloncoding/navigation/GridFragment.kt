package com.example.instargram_cloncoding.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.instargram_cloncoding.R
import com.example.instargram_cloncoding.navigation.DataModle.ContentDTOs
import com.google.firebase.firestore.FirebaseFirestore

class GridFragment : Fragment() {
    var firestore : FirebaseFirestore? = null
    var fragmentView : View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_grid, container, false) //프레그먼트 레이아웃 받아오는 부분
        firestore = FirebaseFirestore.getInstance()
        fragmentView?.findViewById<RecyclerView>(R.id.gridfragment_recyclerview)?.adapter = UserFragmentRecyclerViewAdapter()
        //https://lakue.tistory.com/56 그리드 레이아웃설명
        fragmentView?.findViewById<RecyclerView>(R.id.gridfragment_recyclerview)?.layoutManager = GridLayoutManager(activity,3)
        return fragmentView

    }
    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var contentDTO : ArrayList<ContentDTOs> = arrayListOf()
        init {
            firestore?.collection("images")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot == null) return@addSnapshotListener

                //Data 받아오기
                for (snapshot in querySnapshot.documents) {
                    contentDTO.add(snapshot.toObject(ContentDTOs::class.java)!!)
                }
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

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView){

        }

        override fun getItemCount(): Int {
            return contentDTO.size
        }


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            //담을 그릇 생성
            var imageView = (holder as CustomViewHolder).imageView
            //이미지를 이미지 컨텍스트에 넣어주고 맞는 포지션의 위치에 넣어준다음 이미지를 가운데로 맞춰준다.
            Glide.with(holder.imageView.context).load(contentDTO[position].imageUrl).apply(
                RequestOptions().centerCrop()).into(imageView)
        }
    }
}
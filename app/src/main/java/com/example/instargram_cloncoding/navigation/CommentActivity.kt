package com.example.instargram_cloncoding.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.instargram_cloncoding.R
import com.example.instargram_cloncoding.navigation.DataModle.ContentDTOs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentActivity : AppCompatActivity() {
    var contentUid : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        contentUid = intent.getStringExtra("contextUid")

        findViewById<RecyclerView>(R.id.comment_recyclerview).adapter = CommentRecyclerViewAdapter()
        findViewById<RecyclerView>(R.id.comment_recyclerview).layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.comment_btn_send)?.setOnClickListener {
            var comment = ContentDTOs.Comment()
            //이메일값 받아오기
            comment.userId = FirebaseAuth.getInstance().currentUser.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = findViewById<EditText>(R.id.comment_edit_message).text.toString()
            comment.timestamp = System.currentTimeMillis()

            //이미지안에 contextUid의 comments에 접근, comments의 도큐맨트를 만들어주고 넣어줌.
            //그리고 그 안에 set을 해주게되면 채팅 메세지가 쌓이게된다.
            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").document().set(comment)

            findViewById<EditText>(R.id.comment_edit_message).setText("")
        }
    }
    inner class CommentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var comments : ArrayList<ContentDTOs.Comment> = arrayListOf()

        init {
            FirebaseFirestore.getInstance()
                .collection("images")
                .document(contentUid!!)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener { value, error ->
                    comments.clear()
                    if(value == null)return@addSnapshotListener

                    for(snapshot in value.documents!!){
                        comments.add(snapshot.toObject(ContentDTOs.Comment::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment,parent,false)
            return CustomViewHolder(view)
        }
        private inner class CustomViewHolder(view : View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView
            view.findViewById<TextView>(R.id.commentviewitem_textview_comment).text = comments[position].comment
            view.findViewById<TextView>(R.id.commentviewitem_textview_profile).text = comments[position].userId

            FirebaseFirestore.getInstance()
                .collection("profileImages")
                .document(comments[position].uid!!)
                .get()
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        var url = task.result!!["image"]
                        Glide.with(holder.itemView.context).load(url)
                            .apply(RequestOptions().circleCrop())
                            .into(view.findViewById(R.id.commentviewtitem_imageview_profile))
                    }
                }
        }

        override fun getItemCount(): Int {
           return comments.size
        }

    }
}
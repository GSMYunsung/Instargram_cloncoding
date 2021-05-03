package com.example.instargram_cloncoding.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instargram_cloncoding.R
import com.example.instargram_cloncoding.databinding.ItemDetailBinding
import com.example.instargram_cloncoding.navigation.DataModle.ContentDTOs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

//파이어베이스가 와이파이 문제도 포함되어있나보다.. 맥만..?
//값을 전달해주는 파라미터에도 접근하지 못할뿐더러 그 과정을 실행을못해 프레그먼트도 연결이 안되는 현상이 발견!


private lateinit var binding : ItemDetailBinding

var uid : String? = null

class DetailViewFragment : Fragment(){
    //데이터베이스에 저장하기 위해
    var firestore : FirebaseFirestore? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.item_detail,container,false)
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail,container,false)//프레그먼트 레이아웃 받아오는 부분
        firestore = FirebaseFirestore.getInstance()



        view.findViewById<RecyclerView>(R.id.DetailViewfFagment_Recyclerview).adapter = DetailViewRecyclerViewAdapter()
        view.findViewById<RecyclerView>(R.id.DetailViewfFagment_Recyclerview).layoutManager = LinearLayoutManager(activity)


        return view
    }

    //디테일뷰의 아이템과 디테일뷰 layout을 합치는 어뎁터
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTOs> = arrayListOf()
        var contentUIDList : ArrayList<String> = arrayListOf()

        init {
            //시간순으로 이미지들을 받아오고 만약 연결된 주소의 데이터가 바뀌면 바때마다 자동으로 업데이트해줌
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUIDList.clear()

                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTOs::class.java)
                    contentDTOs.add(item!!)
                    contentUIDList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }
        //화면을 최초 로딩하여 만들어진 View가 없는 경우, xml파일을 inflate하여 ViewHolder를 생성한다.
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail,parent,false)
            return CustomViewHolder(view)
        }

        //굳이 클래스를 만들어서 리턴해주는이유는 메모리사용량때문에 그렇다. 딱히 사용하지 않아도 문제는 되지 않는다.
        inner class CustomViewHolder(view: View): RecyclerView.ViewHolder(view)

        //위의 onCreateViewHolder에서 만든 view와 실제 입력되는 각각의 데이터를 연결한다.
        //서버에서 넘어온 데이터들을 연결!
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            val viewholder = (holder as CustomViewHolder).itemView

            viewholder.findViewById<TextView>(R.id.detailviewitem_profile_nameTextview).text = contentDTOs!![position].userId

            //Image
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).into(viewholder.findViewById(R.id.detailviewitem_imageview_content))
            //Explain of content
            viewholder.findViewById<TextView>(R.id.detailviewitem_explan_textview).text = contentDTOs!![position].explain

            //likes
            viewholder.findViewById<TextView>(R.id.detailviewitem_favoriteconter_textview).text = "Likes" + contentDTOs!![position].favoriteCount

            // 좋아 버튼클릭 이벤트좋아요
            viewholder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview).setOnClickListener {
                favoriteEvent(position)
            }

            if (contentDTOs!![position].favorites.containsKey(uid)){
                //좋아요 버튼이 클릭된부분
                viewholder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview).setImageResource(R.drawable.ic_favorite)
            }
            else
            {
                //클릭이 되지 않은 부분
                viewholder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview).setImageResource(R.drawable.ic_favorite_border)
            }
        }
        //RecyclerView로 만들어지는 item의 총 개수를 반환한다.
        override fun getItemCount(): Int {
            return contentDTOs.size
        }
        fun favoriteEvent(position: Int){
            var tsDoc = firestore?.collection("images")?.document(contentUIDList[position])
            //데이터 가져오기위한 코드
            firestore?.runTransaction{transition ->

                uid = FirebaseAuth.getInstance().currentUser?.uid
                var contentDTOs = transition.get(tsDoc!!).toObject(ContentDTOs::class.java)

                if(contentDTOs!!.favorites.containsKey(uid)){
                    //좋아요 버튼이 클릭되어있을때
                    contentDTOs?.favoriteCount= contentDTOs?.favoriteCount -1
                    contentDTOs?.favorites.remove(uid)
                }
                else
                {
                    //좋아요버튼이 클릭되어있지 않을
                    contentDTOs?.favoriteCount = contentDTOs?.favoriteCount+1
                    contentDTOs?.favorites[uid!!] = true
                }
                //실시간으로 값이 반영되게 하기위해서 다시 트렌지션을 돌려준다.
                transition.set(tsDoc,contentDTOs)
            }
        }
    }



}

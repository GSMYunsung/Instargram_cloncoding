package com.example.instargram_cloncoding.navigation

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.instargram_cloncoding.LoginActivity
import com.example.instargram_cloncoding.MainActivity
import com.example.instargram_cloncoding.R
import com.example.instargram_cloncoding.navigation.DataModle.ContentDTOs
import com.example.instargram_cloncoding.navigation.DataModle.FollowDTO
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("DEPRECATION")
class UserFragment : Fragment() {

    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var auth : FirebaseAuth? = null
    var curruntUserUid : String? = null

    companion object{
        var PICK_PROFILE_FROM_ALBUM = 10
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false) //프레그먼트 레이아웃 받아오기
        uid = arguments?.getString("destinationUid")
        //파이어베이스 초기화, auth 초기화
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        //지금 들어온 프로플창이 내 프로필인지 다른사람 프로필인지 검사하는 곳
        curruntUserUid = auth?.currentUser?.uid

        if(uid == curruntUserUid){
            fragmentView?.findViewById<TextView>(R.id.account_btn_follow_signout)?.text = getString(R.string.signout)
            fragmentView?.findViewById<TextView>(R.id.account_btn_follow_signout)?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity,LoginActivity::class.java))
                auth?.signOut()
            }
        }
        else{
            fragmentView?.findViewById<TextView>(R.id.account_btn_follow_signout)?.text = getString(R.string.follow)
            var mainactivity = (activity as MainActivity)
            mainactivity?.findViewById<TextView>(R.id.toolbar_user_name).text = arguments?.getString("userId")
            mainactivity?.findViewById<ImageView>(R.id.toolbar_btn_back).setOnClickListener {
                mainactivity.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.action_home
            }
            //toolbar이미지를 안보이게!!
            mainactivity?.findViewById<ImageView>(R.id.toolbar_title_image)?.visibility = View.GONE
            mainactivity?.findViewById<TextView>(R.id.toolbar_user_name)?.visibility = View.VISIBLE
            mainactivity?.findViewById<ImageView>(R.id.toolbar_btn_back)?.visibility = View.VISIBLE
            fragmentView?.findViewById<Button>(R.id.account_btn_follow_signout)?.setOnClickListener {
                requestFollow()
            }
        }


        fragmentView?.findViewById<RecyclerView>(R.id.account_recyclerview)?.adapter = UserFragmentRecyclerViewAdapter()
        //한 행에 세개씩 뜰 수 있도록!
        fragmentView?.findViewById<RecyclerView>(R.id.account_recyclerview)?.layoutManager = GridLayoutManager(activity!!,3)
        //fragmentView?.findViewById<LinearLayout>(R.id.content_iv_profile)


        //프로필 사진 올리는부분
            fragmentView?.findViewById<ImageView>(R.id.content_iv_profile)?.setOnClickListener {
                var photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                activity?.startActivityForResult(photoPickerIntent,PICK_PROFILE_FROM_ALBUM)
            }
            getProfileImage()
            getFollowerAndFollowing()
        return fragmentView
    }

    fun getFollowerAndFollowing(){
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener{documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
            if(followDTO?.followingCount != null){
                fragmentView?.findViewById<TextView>(R.id.account_tv_following_count)?.text = followDTO?.followingCount?.toString()
            }
            if(followDTO?.followerCount != null){
                fragmentView?.findViewById<TextView>(R.id.account_tv_fllow_count)?.text = followDTO?.followerCount?.toString()
                //팔로워에 이미 있다면?!?!?
                if(followDTO?.followers?.containsKey(curruntUserUid!!)){
                    fragmentView?.findViewById<Button>(R.id.account_btn_follow_signout)?.text = getString(R.string.follow_cancel)
                    fragmentView?.findViewById<Button>(R.id.account_btn_follow_signout)?.background?.setColorFilter(ContextCompat.getColor(activity!!,R.color.colorLightGray),PorterDuff.Mode.MULTIPLY)
                }
                //팔로워에 들어가있지 않다면?!??
                else{
                    if(uid != curruntUserUid){
                        fragmentView?.findViewById<Button>(R.id.account_btn_follow_signout)?.text = getString(R.string.follow)
                        fragmentView?.findViewById<Button>(R.id.account_btn_follow_signout)?.background?.colorFilter = null
                    }
                }
            }

        }
    }

    fun requestFollow(){
        //상대방이 나한테

        var tsDocFollowing = firestore?.collection("users")?.document(curruntUserUid!!)
        firestore?.runTransaction{ transition ->
            //runTransaction을 사용하지 않을경우 동시에 일어난 상황에 대처하지 못한다.
            //사용자에게 따로따로 읽고 쓰기 권한을 주는것이라 생각하면 편함
            //FollowDTO 캐스팅
            var followDTO = transition.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1
                //중복방지를 위해서 상대방 UID를 넣어준다.
                followDTO!!.followins[uid!!] = true
                //DB로 저장
                transition.set(tsDocFollowing,followDTO)
                //transaction 닫기
                return@runTransaction
            }

            //내 계정에 팔로우가 안되어있는경우
            //특정 키가 있는지 검사한다(followins에)
            if(followDTO.followins.containsKey(uid))
            {
                //팔러워를 한명 줄인다.
                followDTO?.followingCount = followDTO.followingCount-1
                //팔로우를 끊어 준다.
                followDTO?.followins?.remove(uid)

            }
            //내 계정에 팔로우가 되어있는경우
            else{
                //팔러워를 한명 늘린다.
                followDTO?.followingCount = followDTO.followingCount+1
                //팔로우 유저의 uid를 넘겨준다.
                followDTO?.followins[uid!!] = true
            }
            //DB로 저장
            transition.set(tsDocFollowing,followDTO)
            //transaction 닫기
            return@runTransaction
        }

        //----------------------------------------------------------------------------------------------------------

        //내가 상대방한테!

        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction{transition ->
            //FollowDTO를 읽어온다. Follower라는 변수에 FollowDTO를 매핑해준거라 생각함 편함
            var followDTO = transition.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[curruntUserUid!!] = true

                transition.set(tsDocFollower,followDTO!!)
                return@runTransaction
            }

            //내가 상대 계정에 팔로우를 했을경우
            if(followDTO!!.followers.containsKey(curruntUserUid))
            {
                followDTO!!.followerCount  = followDTO!!.followerCount-1
                followDTO!!.followers.remove(curruntUserUid!!)
            }
            //상대가 내 계정에 팔로우를 안했을경우
            else
            {
                followDTO!!.followerCount  = followDTO!!.followerCount+1
                followDTO!!.followers[curruntUserUid!!] = true
            }

            transition.set(tsDocFollower,followDTO!!)
            return@runTransaction
        }


    }
    fun getProfileImage(){
        firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            if(documentSnapshot.data != null){
                //이미지 주소를 불러오고, 이미지를 로딩한다!
                var url = documentSnapshot?.data!!["image"]
                Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop()).into(fragmentView?.findViewById(R.id.content_iv_profile)!!)
            }
        }
    }

    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var contentDTO : ArrayList<ContentDTOs> = arrayListOf()
        init {
            //UID값을 가져와서 자신이 올린 사진만 가져오게 설정함.
            firestore?.collection("images")?.whereEqualTo("uid", uid)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot == null) return@addSnapshotListener

                //Data 받아오기
                for (snapshot in querySnapshot.documents) {
                    contentDTO.add(snapshot.toObject(ContentDTOs::class.java)!!)
                }
                //contentDTO값 받아와서 게시글 수 세기
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

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView){

        }

        override fun getItemCount(): Int {
            return contentDTO.size
        }


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            //담을 그릇 생성
            var imageView = (holder as CustomViewHolder).imageView
            //이미지를 이미지 컨텍스트에 넣어주고 맞는 포지션의 위치에 넣어준다음 이미지를 가운데로 맞춰준다.
            Glide.with(holder.imageView.context).load(contentDTO[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageView)
        }
    }
}
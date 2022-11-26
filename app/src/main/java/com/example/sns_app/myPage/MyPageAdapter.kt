package com.example.sns_app.myPage


import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.example.sns_app.Posting.PostingData
import com.example.sns_app.R
import com.example.sns_app.databinding.PostLayoutBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

// 리사이클러뷰 어댑터
class MyPageAdapter : RecyclerView.Adapter<MyPageAdapter.ViewHolder>() {
    private var items: ArrayList<PostingData> = arrayListOf()
    private var postUids : ArrayList<String> = arrayListOf() // postUid List
    private val storage = Firebase.storage
    private val db = Firebase.firestore
    private val usersInformationRef = db.collection("usersInformation")
    private val currentUid = Firebase.auth.currentUser!!.uid

    init { // 어댑터 연결 시 어댑터에서 게시글 정보를 가져옴
        db.collection("posting").orderBy("time", Query.Direction.DESCENDING).get().addOnSuccessListener {
            items.clear() // 기존 리스트 초기화
            postUids.clear() // 기존 리스트 초기화
            if (it != null) { // 게시글 존재
                for (doc in it) {
                    if (doc["uid"] == currentUid) { // 본인 게시글만 리스트에 추가
                        val item = doc.toObject(PostingData::class.java)
                        items.add(item)
                        postUids.add(doc.id)
                    }
                }
                notifyDataSetChanged()
            } else { // 게시글 없음
                println("null")
            }
        }
    }

    inner class ViewHolder(val binding: PostLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val publisherId = binding.publisherId // onBindViewHolder 에 전달
        val publisherId2 = binding.publisherId2
        val postContent = binding.postContent
        val postImg = binding.postImg
        val publisherImg = binding.publisherImg
        val myImg = binding.myImg
        val favoriteBtn = binding.favoriteBtn
        fun bind() { // 신경 안써도 되는 부분, 아이템 자체 클릭 리스너 ?
            val pos = adapterPosition

            if(pos != RecyclerView.NO_POSITION) {
                itemView.setOnClickListener { }
             }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PostLayoutBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) { // 데이터 바인딩
        val requestManager: RequestManager = Glide.with(holder.binding.root)
        val postImgRef = storage.getReference("PostingImage/${items[position].imageURL}") // 아이템의 위치 파악 가능
        holder.publisherId.text = items[position].userID
        holder.publisherId2.text = items[position].userID
        holder.postContent.text = items[position].context
        postImgRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            requestManager.load(bmp).into(holder.postImg)
        }
        usersInformationRef.document(items[position].UID).get().addOnSuccessListener {
            val filename = it["profileImage"].toString() // 파일 이름을 받아와서
            if (it["profileImage"].toString() == "default") { // profileImage 필드의 값이 default라면
                requestManager.load(R.drawable.profile)
                    .into(holder.binding.publisherImg)// default 프로필 이미지로 변경
            } else {
                val profileImgRef =
                    storage.getReference("ProfileImage/${filename}") // 유저 정보의 파일 정보 참조 획득
                displayImageRef(profileImgRef, holder.binding, holder.publisherImg)
            }
        }

        usersInformationRef.document(currentUid).get().addOnSuccessListener { // 유저 정보 받아오기
            val filename = it["profileImage"].toString() // 파일 이름을 받아와서
            if (it["profileImage"].toString() == "default") { // profileImage 필드의 값이 default라면
                requestManager.load(R.drawable.profile)
                    .into(holder.binding.myImg)// default 프로필 이미지로 변경
            } else {
                val profileImgRef = storage.getReference("ProfileImage/${filename}") // 유저 정보의 파일 정보 참조 획득
                displayImageRef(profileImgRef, holder.binding,  holder.myImg)
            }
        }
        holder.favoriteBtn.setOnClickListener { // 위치 파악 예시, 게시글 자동 id 확인 가능
            println(postUids[position] + " &&&&&&&&&&")
        }
        holder.bind()
    }

    override fun getItemCount() = items.size

    private fun displayImageRef(imageRef: StorageReference?, binding: PostLayoutBinding, view: ImageView) { // 이미지를 화면에 띄움
        val requestManager : RequestManager = Glide.with(binding.root)
        imageRef?.getBytes(Long.MAX_VALUE)?.addOnSuccessListener {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
//            view.setImageBitmap(bmp)
            requestManager.load(bmp).apply(RequestOptions.circleCropTransform()).into(view) // Glide 라이브러리 활용, Circle shape
        }?.addOnFailureListener {
            // Failed to download the image
        }
    }
}
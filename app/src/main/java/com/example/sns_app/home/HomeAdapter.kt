package com.example.sns_app.home

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.example.sns_app.posting.PostingData
import com.example.sns_app.R
import com.example.sns_app.databinding.PostLayoutBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

// 리사이클러뷰 어댑터
class HomeAdapter : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {
    private var items: List<PostingData> = emptyList()
    private val storage = Firebase.storage
    private val db = Firebase.firestore
    private val usersInformationRef = db.collection("usersInformation")
    private val currentUid = Firebase.auth.currentUser!!.uid

    inner class ViewHolder(private val binding: PostLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        private val requestManager: RequestManager = Glide.with(binding.root) // Glide 에러 해결책
        fun bind(item: PostingData) {
            val postImgRef = storage.getReference("PostingImage/${item.imageURL}")
            binding.publisherId.text = item.userID
            binding.publisherId2.text = item.userID
            binding.postContent.text = item.context
            postImgRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {
                val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                requestManager.load(bmp).into(binding.postImg)
            }
            usersInformationRef.document(item.UID).get().addOnSuccessListener {
                val filename = it["profileImage"].toString() // 파일 이름을 받아와서
                if (it["profileImage"].toString() == "default") { // profileImage 필드의 값이 default라면
                    requestManager.load(R.drawable.profile)
                        .into(binding.publisherImg)// default 프로필 이미지로 변경
                } else {
                    val profileImgRef =
                        storage.getReference("ProfileImage/${filename}") // 유저 정보의 파일 정보 참조 획득
                    displayImageRef(profileImgRef, binding, binding.publisherImg)
                }
            }

            usersInformationRef.document(currentUid).get().addOnSuccessListener { // 유저 정보 받아오기
                val filename = it["profileImage"].toString() // 파일 이름을 받아와서
                if (it["profileImage"].toString() == "default") { // profileImage 필드의 값이 default라면
                    requestManager.load(R.drawable.profile)
                        .into(binding.myImg)// default 프로필 이미지로 변경
                } else {
                    val profileImgRef = storage.getReference("ProfileImage/${filename}") // 유저 정보의 파일 정보 참조 획득
                    displayImageRef(profileImgRef, binding, binding.myImg)
                }
            }

            binding.favoriteBtn.isChecked = item.heartClickPeople.containsKey(currentUid)

            binding.favoriteBtn.setOnClickListener {
                db.collection("posting").whereEqualTo("imageURL",item.imageURL).get().addOnSuccessListener {
                    for(doc in it) {
                        favoriteEvent(doc.id)
                    }
                }
            }

            binding.heartCount.text = item.heartCount.toString()

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PostLayoutBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun setDataList(list: List<PostingData>) {
        items = list
        notifyDataSetChanged()
    }

    private fun favoriteEvent(postUid: String) {
        val tsDoc = db.collection("posting").document(postUid)
        db.runTransaction { transition ->
            val postDto = transition.get(tsDoc).toObject(PostingData::class.java)

            // 좋아요가 눌린 경우
            if (postDto!!.heartClickPeople.containsKey(currentUid)) {
                postDto.heartCount -= 1
                postDto.heartClickPeople.remove(currentUid)
            } else {    // 눌리지 않은 경우
                postDto.heartCount += 1
                postDto.heartClickPeople[currentUid] = true
            }

            transition.set(tsDoc, postDto)
        }
    }

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
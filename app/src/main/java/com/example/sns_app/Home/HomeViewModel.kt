package com.example.sns_app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sns_app.Home.FollowDto
import com.example.sns_app.Posting.PostingData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeViewModel : ViewModel() {
    private val _posts = MutableLiveData<List<PostingData>>()
    val posts: LiveData<List<PostingData>> get() = _posts

    private val db = Firebase.firestore
    private val itemsCollectionRef = db.collection("posting")

    fun getFollowInfo(followDto: FollowDto) { // 팔로우 데이터를 받으면
        createList(followDto.followings.keys) // 팔로잉한 유저 목록을 createList 에 전달
    }

    private fun createList(uidList: MutableSet<String>) { // 유저 목록을 전달 받음
        itemsCollectionRef.get().addOnSuccessListener { // 게시글 정보를 참조
            val list: MutableList<PostingData> = mutableListOf() // createList 호출 시 리스트 초기화, 기존 문제 해결 부분
            for (doc in it) { // 게시글 정보 중에서
                for (uid in uidList) { 
                    if (doc["uid"].toString() == uid) // 팔로잉한 유저 리스트를 찾으면
                        list.add( // 리스트에 추가
                            PostingData(
                                context = doc["context"].toString(),
                                imageURL = doc["imageURL"].toString(),
                                time = doc["time"].toString(),
                                userID = doc["userID"].toString(),
                                UID = doc["uid"].toString(),
                                profileURL = doc["profileURL"].toString(),
                                heartClickPeople = hashMapOf(),
                                heartCount = 0
                            )
                        )
                    }
                }
            _posts.value = list // 라이브 데이터에 직접 리스트 전달 ( SuccessListener 내부에서 ), 기존 문제 해결 부분
        }
    }
}

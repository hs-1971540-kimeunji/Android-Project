package com.example.sns_app.posting

// 글 내용
data class PostingData( // 어댑터의 init 부분에서 발생하는 오류를 잡기위해 초기화
    var context: String = "",
    var imageURL: String = "", // 사진 url
    var UID: String = "", // 고유 UID
    var userID: String = "", // 글을 작성한 유저의 아이디
    var profileURL: String = "",
    var time: String = "", // 글을 올린 시간
    var heartCount: Int = 0, // 해당 글의 하트 수(좋아요)
    var heartClickPeople: MutableMap<String, Boolean> = HashMap()) { // 하트 누른 사람 관리

    // 해당 글의 댓글 관리 (아직 사용안함)
    data class Comment(var UID: String,
                       var userID: String,
                       var comment: String,
                       var time: String)
}
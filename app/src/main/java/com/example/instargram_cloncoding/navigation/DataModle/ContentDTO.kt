package com.example.instargram_cloncoding.navigation.DataModle

//변수 클래스

class ContentDTO(var exception: String? = null,
                 var imageUri : String? = null, // 이미지 주소 관리 변수
                 var uid : String? = null, // 유저 아이디 관리 변수
                 var userId : String? = null, //올린 유저의 이미지를 관리해주는 변수
                 var timestemp : Long? = null,//몇시 몇분에 사용자가 자료를 올렸는지 관리하는 변수
                 var favoriteCount : Int = 0, // 좋아요 개수 카운트
                 var favorites : Map<String,Boolean> = HashMap() //중복좋아요를 관리하기위해 좋아요를 누른 유저관리 변수
){
    data class Comment(var uid : String? = null,
                       var userId: String? = null,
                       var comment: String? = null,
                       var timestemp: String? = null)
}
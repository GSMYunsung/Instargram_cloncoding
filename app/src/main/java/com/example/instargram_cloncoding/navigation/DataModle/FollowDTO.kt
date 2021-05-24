package com.example.instargram_cloncoding.navigation.DataModle


data class FollowDTO(
        var followerCount : Int = 0,
        //두가지 변수형
        var followers : MutableMap<String,Boolean> = HashMap(),

        var followingCount : Int = 0,
        var followins : MutableMap<String,Boolean> = HashMap(),
)
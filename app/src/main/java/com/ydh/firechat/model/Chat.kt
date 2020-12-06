package com.ydh.firechat.model

data class Chat(
        var senderId:String = "",
        var receiverId:String = "",
        var message:String = ""
)
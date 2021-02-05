package kerobot.android.holoduleapp.model

import com.google.gson.annotations.SerializedName

data class Auth (
    @SerializedName("username") var username : String,
    @SerializedName("password") var password : String
)

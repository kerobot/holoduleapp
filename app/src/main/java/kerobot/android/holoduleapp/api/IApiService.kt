package kerobot.android.holoduleapp.api

import kerobot.android.holoduleapp.model.Auth
import kerobot.android.holoduleapp.model.Token
import kerobot.android.holoduleapp.model.Result
import retrofit2.http.*

interface IApiService {
    @Headers("Content-Type: application/json")
    @POST("holoapi/auth")
    suspend fun createToken(@Body auth: Auth): Token

    @Headers("Content-Type: application/json")
    @GET("holoapi/holodules/{date}")
    suspend fun getHolodules(@Header("Authorization") jwtToken: String,
                     @Path("date") dateString: String): Result
}

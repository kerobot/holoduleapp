package kerobot.android.holoduleapp.api

import kerobot.android.holoduleapp.model.Auth
import kerobot.android.holoduleapp.model.Holodule
import kerobot.android.holoduleapp.model.Token
import retrofit2.http.*

interface IApiService {
    // Web API の JWT トークンを取得（POST）
    @Headers("Content-Type: application/json")
    @POST("holoapi/login")
    suspend fun createToken(@Body auth: Auth): Token

    // JWT トークンを指定してホロジュールの配信予定を取得（GET）
    @Headers("Content-Type: application/json")
    @GET("holoapi/holodules/{date}")
    suspend fun getHolodules(@Header("Authorization") jwtToken: String,
                     @Path("date") dateString: String): List<Holodule>
}

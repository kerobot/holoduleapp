package kerobot.android.holoduleapp.api

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val httpBuilder: OkHttpClient.Builder get() {
    // HTTP client の作成
    val timeout:Long = 30
    val httpClient = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
                .header("Accept", "application/json")
                .method(original.method, original.body)
                .build()
        return@Interceptor chain.proceed(request)
    }).readTimeout(timeout, TimeUnit.SECONDS)
    // ログの設定
    val loggingInterceptor = HttpLoggingInterceptor()
    loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    httpClient.addInterceptor(loggingInterceptor)
    return httpClient
}

fun <S> create(serviceClass: Class<S>, baseUrl: String): S {
    // gson の作成
    val gson = GsonBuilder().serializeNulls().create()
    // retrofit の作成
    val retrofit = Retrofit.Builder()
            // 基本 URL の指定
            .baseUrl(baseUrl)
            // コンバーターとして Gson を利用
            .addConverterFactory(GsonConverterFactory.create(gson))
            // カスタマイズした HTTP client を指定
            .client(httpBuilder.build())
            // リクエストの組み立て
            .build()
    // Interface から実装を取得
    return retrofit.create(serviceClass)
}

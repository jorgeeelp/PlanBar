package com.losjorges.planbar.network

import com.losjorges.planbar.models.Empleado
import com.losjorges.planbar.models.LoginResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("get_empleados.php")
    fun getEmpleados(): Call<List<Empleado>>

    @FormUrlEncoded
    @POST("login_admin.php")
    fun loginAdmin(@Field("pass") pass: String): Call<LoginResponse>
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2/login_api/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
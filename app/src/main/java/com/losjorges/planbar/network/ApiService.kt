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
    // 1. Obtener empleados para la pantalla de selección (sin admin)
    @GET("get_empleados.php")
    fun getEmpleados(): Call<List<Empleado>>

    // 2. Obtener TODOS los empleados para el Administrador
    @GET("get_empleados_admin.php")
    fun getEmpleadosAdmin(): Call<List<Empleado>>

    // 3. Login del administrador
    @FormUrlEncoded
    @POST("login_admin.php")
    fun loginAdmin(@Field("pass") pass: String): Call<LoginResponse>

    // 4. Insertar nuevo empleado (ESTA ES NUEVA)
    @FormUrlEncoded
    @POST("insert_empleado.php")
    fun insertEmpleado(
        @Field("dni") dni: String,
        @Field("nombre") nombre: String,
        @Field("rol") rol: String
    ): Call<LoginResponse>

    // 5. Eliminar empleado (ESTA ES LA QUE TE FALTA)
    @FormUrlEncoded
    @POST("delete_empleado.php")
    fun deleteEmpleado(@Field("id") id: Int): Call<LoginResponse>

    @FormUrlEncoded
    @POST("update_empleado.php")
    fun updateEmpleado(
        @Field("id") id: Int,
        @Field("dni") dni: String,
        @Field("nombre") nombre: String,
        @Field("rol") rol: String
    ): Call<LoginResponse>
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2/api/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
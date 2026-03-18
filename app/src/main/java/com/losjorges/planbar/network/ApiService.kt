package com.losjorges.planbar.network

import com.losjorges.planbar.models.Empleado
import com.losjorges.planbar.models.LoginResponse
import com.losjorges.planbar.models.Mesa
import com.losjorges.planbar.models.Producto
import com.losjorges.planbar.models.Reserva
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @FormUrlEncoded
    @POST("login_admin.php")
    fun loginAdmin(@Field("pass") pass: String): Call<LoginResponse>

    //empleado
    @GET("get_empleados.php")
    fun getEmpleados(): Call<List<Empleado>>

    @GET("get_empleados_admin.php")
    fun getEmpleadosAdmin(): Call<List<Empleado>>

    @FormUrlEncoded
    @POST("insert_empleado.php")
    fun insertEmpleado(
        @Field("dni") dni: String,
        @Field("nombre") nombre: String,
        @Field("rol") rol: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("update_empleado.php")
    fun updateEmpleado(
        @Field("id") id: Int,
        @Field("dni") dni: String,
        @Field("nombre") nombre: String,
        @Field("rol") rol: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("delete_empleado.php")
    fun deleteEmpleado(@Field("id") id: Int): Call<LoginResponse>

    //mesas
    @GET("get_mesas.php")
    fun getMesas(): Call<List<Mesa>>

    @FormUrlEncoded
    @POST("insert_mesa.php")
    fun insertMesa(
        @Field("numero") numero: Int,
        @Field("capacidad") capacidad: Int
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("update_mesa.php")
    fun updateMesa(
        @Field("id") id: Int,
        @Field("numero") numero: Int,
        @Field("capacidad") capacidad: Int
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("delete_mesa.php")
    fun deleteMesa(@Field("id") id: Int): Call<LoginResponse>

    //productos
    @GET("get_productos.php")
    fun getProductos(): Call<List<Producto>>

    @FormUrlEncoded
    @POST("insert_producto.php")
    fun insertProducto(
        @Field("nombre") nombre: String,
        @Field("precio") precio: Double,
        @Field("categoria") categoria: String,
        @Field("observaciones") observaciones: String,
        @Field("foto") foto: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("update_producto.php")
    fun updateProducto(
        @Field("id") id: Int,
        @Field("nombre") nombre: String,
        @Field("precio") precio: Double,
        @Field("categoria") categoria: String,
        @Field("observaciones") observaciones: String,
        @Field("foto") foto: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("delete_producto.php")
    fun deleteProducto(@Field("id") id: Int): Call<LoginResponse>

    //reservas
    @GET("get_reservas.php")
    fun getReservas(): Call<List<Reserva>>
}

object RetrofitClient {
    private const val BASE_URL = "http://planbar.atwebpages.com"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
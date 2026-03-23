package com.losjorges.planbar.network

import com.losjorges.planbar.models.Empleado
import com.losjorges.planbar.models.LoginResponse
import com.losjorges.planbar.models.Mesa
import com.losjorges.planbar.models.Producto
import com.losjorges.planbar.models.Reserva
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface ApiService {
    @FormUrlEncoded
    @POST("login_admin.php")
    fun loginAdmin(@Field("pass") pass: String): Call<LoginResponse>

    //empleado
    @GET("empleados/get_empleados.php")
    fun getEmpleados(): Call<List<Empleado>>

    @GET("empleados/get_empleados_admin.php")
    fun getEmpleadosAdmin(): Call<List<Empleado>>

    @FormUrlEncoded
    @POST("empleados/insert_empleado.php")
    fun insertEmpleado(
        @Field("dni") dni: String,
        @Field("nombre") nombre: String,
        @Field("rol") rol: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("empleados/update_empleado.php")
    fun updateEmpleado(
        @Field("id") id: Int,
        @Field("dni") dni: String,
        @Field("nombre") nombre: String,
        @Field("rol") rol: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("empleados/delete_empleado.php")
    fun deleteEmpleado(@Field("id") id: Int): Call<LoginResponse>

    //mesas
    @GET("mesas/get_mesas.php")
    fun getMesas(): Call<List<Mesa>>

    @FormUrlEncoded
    @POST("mesas/insert_mesa.php")
    fun insertMesa(
        @Field("numero") numero: Int,
        @Field("capacidad") capacidad: Int
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("mesas/update_mesa.php")
    fun updateMesa(
        @Field("id") id: Int,
        @Field("numero") numero: Int,
        @Field("capacidad") capacidad: Int
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("mesas/delete_mesa.php")
    fun deleteMesa(@Field("id") id: Int): Call<LoginResponse>

    @FormUrlEncoded
    @POST("mesas/update_mesa_posicion.php")
    fun updateMesaPosicion(
        @Field("id") id: Int,
        @Field("posX") posX: Float,
        @Field("posY") posY: Float
    ): Call<LoginResponse>

    //productos
    @GET("productos/get_productos.php")
    fun getProductos(): Call<List<Producto>>

    @FormUrlEncoded
    @POST("productos/insert_producto.php")
    fun insertProducto(
        @Field("nombre") nombre: String,
        @Field("precio") precio: Double,
        @Field("categoria") categoria: String,
        @Field("observaciones") observaciones: String,
        @Field("foto") foto: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("productos/update_producto.php")
    fun updateProducto(
        @Field("id") id: Int,
        @Field("nombre") nombre: String,
        @Field("precio") precio: Double,
        @Field("categoria") categoria: String,
        @Field("observaciones") observaciones: String,
        @Field("foto") foto: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("productos/delete_producto.php")
    fun deleteProducto(@Field("id") id: Int): Call<LoginResponse>

    //reservas
    @GET("reservas/get_reservas.php")
    fun getReservas(): Call<List<Reserva>>

    @FormUrlEncoded
    @POST("reservas/cancelar_reserva_por_num.php")
    fun cancelarReservaPorNum(
        @Field("num_reserva") num_reserva: String
    ): Call<LoginResponse>
}

object RetrofitClient {
    private const val BASE_URL = "http://planbar.atwebpages.com"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
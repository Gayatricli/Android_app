package com.example.stressease.Api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

data class ChatRequest(val message: String,val session_id:String?=null)
data class AiResponse(
    val content: String,
    val timestamp: String,
    val role: String
)
data class ChatResponse(val reply: String, val emotion: String,val ai_response: AiResponse?,val session_id:String?)

data class CrisisContact(
    val id: String?,
    val type: String?,
    val name: String?,
    val number: String?,
    val description: String?,
    val website: String?,
    val availability: String?,
    val country: String?,
    val priority: Int?
)
data class EmergencyService(
    val description: String?,
    val number: String?
)
data class CrisisData(@SerializedName("crisis_hotlines") val crisisHotlines: List<CrisisContact>?)
data class CrisisResources(
    val country: String?,
    @SerializedName("crisis_hotlines") val crisisHotlines: List<CrisisContact>?,
    @SerializedName("online_resources") val onlineResources: List<CrisisContact>?,
    @SerializedName("emergency_services") val emergencyServices: EmergencyService?
)

data class CrisisResponse(val success: Boolean,
                         val message: String?,
                         val resources: CrisisResources?)


interface ApiService {
    @POST("api/chat/message") // for real time chat
    suspend fun sendMessage(
        @Header("Authorization") authHeader: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>

    @POST("mood/log") //for sentiment analysis /text extraction and generation
    fun logMood(@Body payload: Map<String, String>): Call<Map<String, String>>

    @GET("api/chat/crisis-resources")
    suspend fun getCrisisContacts(
        @Header("Authorization") authHeader: String,
        @Query("country") country: String
    ): Response<CrisisResponse>
}

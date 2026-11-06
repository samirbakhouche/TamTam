package com.example.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TamTamApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body req: LoginRequest): Response<ApiResponse<AuthData>>

    @POST("api/v1/auth/register")
    suspend fun register(@Body req: RegisterRequest): Response<ApiResponse<AuthData>>

    @GET("api/v1/users")
    suspend fun getUsers(): Response<ApiResponse<List<UserDto>>>

    @GET("api/v1/users/{id}")
    suspend fun getUserProfile(@Path("id") userId: Long): Response<ApiResponse<UserDto>>

    @POST("api/v1/users/presence")
    suspend fun updatePresence(@Body req: PresenceRequest): Response<ApiResponse<Unit>>

    @GET("api/v1/conversations")
    suspend fun getConversations(): Response<ApiResponse<List<ConversationDto>>>

    @POST("api/v1/conversations/direct")
    suspend fun createDirectConversation(@Body req: CreateDirectChatRequest): Response<ApiResponse<ConversationDto>>

    @GET("api/v1/conversations/{id}/messages")
    suspend fun getMessages(@Path("id") conversationId: Long): Response<ApiResponse<List<MessageDto>>>

    @POST("api/v1/conversations/{id}/messages")
    suspend fun sendMessage(
        @Path("id") conversationId: Long,
        @Body req: SendMessageRequest
    ): Response<ApiResponse<MessageDto>>
}

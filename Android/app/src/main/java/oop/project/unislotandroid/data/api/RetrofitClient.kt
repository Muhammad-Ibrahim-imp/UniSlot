package oop.project.unislotandroid.data.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import oop.project.unislotandroid.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// DataStore extension on Context
// Extension property on Context that creates a single instance of DataStore
// using the Preferences API. It stores key-value pairs persistently in a file
// named "auth_prefs". The 'by preferencesDataStore' delegate ensures that
// only one DataStore instance is created and shared across the entire app.
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

// Singleton object that defines all keys used for storing and retrieving
// user-related data in DataStore (Preferences).
object TokenKeys {
    val TOKEN = stringPreferencesKey("jwt_token")
    val EMAIL = stringPreferencesKey("email")
    val ROLE  = stringPreferencesKey("role")
}

/**
 * TokenStorage is a wrapper class around DataStore (Preferences)
 * used to manage and persist user authentication data locally.
 */
class TokenStorage(private val context: Context) {

    /**
     * Emits the current JWT token and updates automatically whenever it changes.
     */
    val tokenFlow: Flow<String?> =
        context.dataStore.data.map { it[TokenKeys.TOKEN] }

    val emailFlow: Flow<String?> =
        context.dataStore.data.map { it[TokenKeys.EMAIL] }

    val roleFlow: Flow<String?> =
        context.dataStore.data.map { it[TokenKeys.ROLE] }

    /**
     * Saves authentication data into DataStore asynchronously.
     */
    suspend fun save(token: String, email: String, role: String) {
        context.dataStore.edit {
            it[TokenKeys.TOKEN] = token
            it[TokenKeys.EMAIL] = email
            it[TokenKeys.ROLE]  = role
        }
    }

    /**
     * Clears all stored authentication data (Logout).
     */
    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    /**
     * Synchronously retrieves the current token value.
     * blocks the thread until value is fetched.
     */
    fun getTokenSync(): String? =
        runBlocking { tokenFlow.first() }
}

object RetrofitClient {

    private lateinit var tokenStorage: TokenStorage

    fun init(context: Context) {
        tokenStorage = TokenStorage(context)
    }

    private val authInterceptor = Interceptor { chain ->
        val token = tokenStorage.getTokenSync()
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * OkHttpClient with increased timeouts to 1 minute.
     * This ensures that Render's cold start (spinning up from sleep)
     * does not trigger a connection timeout error.
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        // Set timeouts to 60 seconds (1 minute)
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun getTokenStorage() = tokenStorage
}
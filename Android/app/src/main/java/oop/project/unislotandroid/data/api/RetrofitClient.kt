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

// DataStore extension on Context
// Extension property on Context that creates a single instance of DataStore
// using the Preferences API. It stores key-value pairs persistently in a file
// named "auth_prefs". The 'by preferencesDataStore' delegate ensures that
// only one DataStore instance is created and shared across the entire app.
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs") //“Every Context in my app now has a DataStore called auth_prefs.”

// Singleton object that defines all keys used for storing and retrieving
// user-related data in DataStore (Preferences).
// Each key is strongly typed using stringPreferencesKey(), ensuring that
// only String values can be stored/retrieved with these keys.
//
// TOKEN -> key for storing JWT authentication token
// EMAIL -> key for storing user's email address
// ROLE  -> key for storing user's role (e.g., admin, user)
// "jwt_token" = real key (stored in file)
// TOKEN = safe handle to access that key
object TokenKeys {//stringPreferencesKey("jwt_token") creates a type-safe key used by DataStore to store and retrieve a String value associated with "jwt_token".
val TOKEN = stringPreferencesKey("jwt_token")
    val EMAIL = stringPreferencesKey("email")
    val ROLE  = stringPreferencesKey("role")
}

/**
 * TokenStorage is a wrapper class around DataStore (Preferences)
 * used to manage and persist user authentication data locally.
 *
 * It handles storing, retrieving, and clearing JWT token,
 * email, and user role in a type-safe and reactive way.
 *
 * Data is exposed as Flow so UI or ViewModel can observe changes
 * automatically whenever values are updated.
 */
class TokenStorage(private val context: Context) {

    /**
     * Emits the current JWT token and updates automatically whenever it changes
     * in DataStore. If no token exists, emits null.
     */
    //context.dataStore.data
    //This gives you:
    //Flow<Preferences>
    //Meaning:
    //“A continuous stream of all stored key-value data”
    val tokenFlow: Flow<String?> =
        context.dataStore.data.map { it[TokenKeys.TOKEN] }
    /**
     * it = full preferences map
     * it[TokenKeys.TOKEN] = extract only JWT token
     *
     * So:
     *
     * { jwt_token="abc123", email="a@gmail.com" }
     *
     * becomes:
     *
     * "abc123"
     */
    val emailFlow: Flow<String?> =
        context.dataStore.data.map { it[TokenKeys.EMAIL] }
    val roleFlow: Flow<String?> =
        context.dataStore.data.map { it[TokenKeys.ROLE] }

    /**
     * Saves authentication data (token, email, role) into DataStore.
     * This is a suspend function because DataStore performs asynchronous(Concurrent) IO operations.
     */
    suspend fun save(token: String, email: String, role: String) {
        context.dataStore.edit {
            it[TokenKeys.TOKEN] = token
            it[TokenKeys.EMAIL] = email
            it[TokenKeys.ROLE]  = role
        }
    }

    /**
     * Clears all stored authentication data from DataStore.
     * Typically used during logout to remove user session.
     */
    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    /**
     * Synchronously(Sequentially) retrieves the current token value.
     * Internally uses runBlocking + Flow.first() to get a single value.
     *
     * NOTE: Should be used carefully as it blocks the calling thread.
     */
    fun getTokenSync(): String? =
        runBlocking { tokenFlow.first() } //runBlocking = a coroutine builder that blocks the current thread until all suspend code inside it completes.
}

object RetrofitClient {

    private lateinit var tokenStorage: TokenStorage //This declares a variable named tokenStorage that will hold a TokenStorage object, but it will be initialized later.

    fun init(context: Context) {
        tokenStorage = TokenStorage(context)
    }

    private val authInterceptor = Interceptor { chain -> // Interceptor: “A function that runs before every HTTP request and can modify it.”
        // It is part of OkHttp (the networking library behind Retrofit).
        // chain = the object that controls the current HTTP request and lets you modify it or pass it forward.
        val token = tokenStorage.getTokenSync()
        val request = if (token != null) {
            chain.request().newBuilder() //request() simply means: “Give me the current HTTP request that is about to be sent.”
                // newBuilder() means: “Create an editable copy of this HTTP request so you can modify it safely.”
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request) //It means:  “Send this HTTP request forward to the next step (network/server) and continue the request chain.”
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }   //It is a built-in OkHttp tool that:Logs HTTP requests and responses for debugging.
    //So it shows things like:
    //URL
    //headers
    //request body
    //response body
    //status codes

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()
    //This is your network engine configuration. It tells Retrofit/OkHttp:
    //“Before sending any request, run these interceptors.”

    val api: ApiService by lazy { //Kotlin lazy initialization Meaning:“Don’t create this object now. Create it only when it is first used.”
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL) //Sets server address
            .client(okHttpClient) //Attaches your custom client that includes: 1)authInterceptor (JWT token) 2)loggingInterceptor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java) //create(ApiService::class.java) tells Retrofit to generate a runtime implementation of the ApiService interface that performs actual HTTP calls.
    }

    fun getTokenStorage() = tokenStorage //It simply returns the tokenStorage object
}

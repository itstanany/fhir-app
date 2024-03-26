package com.openmrs.openmrsandroidclient.auth

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.openmrs.openmrsandroidclient.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.openid.appauth.connectivity.ConnectionBuilder
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import org.json.JSONException
import java.io.IOException

/**
 * It is about translating auth configuration json file into Kotlin class that can be consumed
 * across the app
 */
class AuthConfiguration private constructor(private val context: Context) {
  private val authConfigKey by lazy { stringPreferencesKey("AuthConfig") }
  private val gson = Gson()

  val authConfigJson: AuthConfigData by lazy {
    try {
      val stringJson =
        context.resources.openRawResource(R.raw.auth_config).bufferedReader().use { it.readText() }
      gson.fromJson(stringJson, AuthConfigData::class.java)
    } catch (ex: IOException) {
      throw AuthConfigUtil.InvalidConfigurationException("Failed to read configuration: " + ex.message)
    } catch (ex: JSONException) {
      throw AuthConfigUtil.InvalidConfigurationException("Unable to parse configuration: " + ex.message)
    }
  }

  val stored: AuthConfigData
    get() {
      val serializedAuth = runBlocking {
        return@runBlocking context.dataStore.data.first()[authConfigKey]
      } ?: return authConfigJson
      return gson.fromJson(serializedAuth, AuthConfigData::class.java)
    }

  suspend fun save() {
    context.dataStore.edit { pref -> pref[authConfigKey] = gson.toJson(authConfigJson) }
  }

  val clientId: String
    get() = authConfigJson.client_id
  val scope: String?
    get() = authConfigJson.authorization_scope
  val redirectUri: Uri?
    get() = Uri.parse(authConfigJson.redirect_uri)
  val discoveryUri: Uri?
    get() = Uri.parse(authConfigJson.discovery_uri)
  val authEndpointUri: Uri?
    get() = Uri.parse(authConfigJson.authorization_endpoint_uri)
  val tokenEndpointUri: Uri?
    get() = Uri.parse(authConfigJson.token_endpoint_uri)
  val registrationEndpointUri: Uri?
    get() = Uri.parse(authConfigJson.registration_endpoint_uri)
  val endSessionEndpoint: Uri?
    get() = Uri.parse(authConfigJson.end_session_endpoint)
  val connectionBuilder: ConnectionBuilder
    get() =
      if (authConfigJson.https_required) {
        DefaultConnectionBuilder.INSTANCE
      } else ConnectionBuilderForTesting

  init {
    AuthConfigUtil.isRequiredConfigString(authConfigJson.client_id)
    AuthConfigUtil.isRequiredConfigString(authConfigJson.authorization_scope)
    AuthConfigUtil.isRequiredConfigUri(authConfigJson.redirect_uri)
    AuthConfigUtil.isRequiredConfigUri(authConfigJson.end_session_redirect_uri)
    AuthConfigUtil.isRedirectUriRegistered(context, authConfigJson.redirect_uri!!)
    if (authConfigJson.discovery_uri == null) {
      AuthConfigUtil.isRequiredConfigWebUri(authConfigJson.authorization_endpoint_uri)
      AuthConfigUtil.isRequiredConfigWebUri(authConfigJson.token_endpoint_uri)
      AuthConfigUtil.isRequiredConfigWebUri(authConfigJson.user_info_endpoint_uri)
      AuthConfigUtil.isRequiredConfigWebUri(authConfigJson.end_session_endpoint)
    }
  }

  companion object {
    @SuppressLint("StaticFieldLeak")
    private var INSTANCE: AuthConfiguration? = null

    @Synchronized
    fun getInstance(context: Context): AuthConfiguration {
      if (INSTANCE == null) {
        INSTANCE= AuthConfiguration(context.applicationContext)
      }
      return INSTANCE as AuthConfiguration
    }
  }
}

data class AuthConfigData(
  val client_id: String,
  val authorization_scope: String?,
  val redirect_uri: String?,
  val end_session_redirect_uri: String?,
  val discovery_uri: String?,
  val authorization_endpoint_uri: String?,
  val token_endpoint_uri: String?,
  val user_info_endpoint_uri: String?,
  val end_session_endpoint: String?,
  val registration_endpoint_uri: String?,
  val https_required: Boolean,
)

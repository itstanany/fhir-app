package com.openmrs.openmrsandroidclient

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import timber.log.Timber

class LoginActivityViewModel constructor(application: Application) : AndroidViewModel(application) {
  private val loginRepository by lazy {
    LoginRepository.getInstance(application.applicationContext)
  }

  suspend fun createIntent(): Intent? {
    loginRepository.hasConfigurationChanged()
    loginRepository.initializeAppAuth()
    return loginRepository.getAuthIntent()
  }

  suspend fun handleLoginResponse(response: AuthorizationResponse?, ex: AuthorizationException?) {
    if (response != null || ex != null) {
      loginRepository.updateAfterAuthorization(response, ex)
    }
    when {
      response?.authorizationCode != null -> {
        loginRepository.exchangeCodeForToken(response, ex)
      }
      ex != null -> {
        Timber.e("Authorization flow failed: " + ex.message)
      }
      else -> {
        Timber.e("No authorization state retained - reauthorization required")
      }
    }
  }
}


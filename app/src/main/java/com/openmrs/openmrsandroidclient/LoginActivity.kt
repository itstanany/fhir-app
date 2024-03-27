package com.openmrs.openmrsandroidclient

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.openmrs.openmrsandroidclient.databinding.ActivityLoginBinding
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import timber.log.Timber


class LoginActivity : AppCompatActivity() {

  private lateinit var binding: ActivityLoginBinding
  private val viewModel: LoginActivityViewModel by viewModels()

  private val getContent =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      lifecycleScope.launchWhenResumed {
        if (result.resultCode == RESULT_OK) {
          Timber.i("Exchange for token")
          val response = result.data?.let { AuthorizationResponse.fromIntent(it) }
          val ex = AuthorizationException.fromIntent(result.data)
          viewModel.handleLoginResponse(response, ex)
          val mainActivityIntent = Intent(this@LoginActivity, MainActivity::class.java)
          startActivity(mainActivityIntent)
        }
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityLoginBinding.inflate(layoutInflater)
    setContentView(binding.root)
    lifecycleScope.launchWhenResumed {
      val loginIntent = viewModel.createIntent()
      binding.buttonLogin.setOnClickListener { getContent.launch(loginIntent) }
    }
  }
}


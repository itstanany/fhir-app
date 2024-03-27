package com.openmrs.openmrsandroidclient

import android.app.Application
import android.content.Context
import com.google.android.fhir.DatabaseErrorStrategy
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.FhirEngineConfiguration
import com.google.android.fhir.FhirEngineProvider
import com.google.android.fhir.ServerConfiguration
import com.google.android.fhir.datacapture.DataCaptureConfig
import com.google.android.fhir.search.search
import timber.log.Timber


class FhirApplication : Application(), DataCaptureConfig.Provider {
    // Only initiate the FhirEngine when used for the first time, not when the app is created.
    private val fhirEngine: FhirEngine by lazy { constructFhirEngine() }

    private var dataCaptureConfig: DataCaptureConfig? =
        DataCaptureConfig(xFhirQueryResolver = { fhirEngine.search(it) })

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        FhirEngineProvider.init(
            FhirEngineConfiguration(
                enableEncryptionIfSupported = true,
                DatabaseErrorStrategy.RECREATE_AT_OPEN,
                ServerConfiguration(
                    ServerConstants.BASE_URL,
                    authenticator = LoginRepository.getInstance(applicationContext)
                )
            )
        )
    }

    private fun constructFhirEngine(): FhirEngine {
        return FhirEngineProvider.getInstance(this)
    }

    companion object {
        fun fhirEngine(context: Context) = (context.applicationContext as FhirApplication).fhirEngine
    }

    override fun getDataCaptureConfig(): DataCaptureConfig = dataCaptureConfig ?: DataCaptureConfig()
}

object ServerConstants {
    const val BASE_URL = "http://10.0.2.2:8080/fhir/"
}

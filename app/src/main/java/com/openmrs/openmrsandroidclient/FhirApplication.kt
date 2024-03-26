package com.openmrs.openmrsandroidclient

import android.app.Application
import com.google.android.fhir.datacapture.DataCaptureConfig
import timber.log.Timber

class FhirApplication: Application(), DataCaptureConfig.Provider {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
    override fun getDataCaptureConfig(): DataCaptureConfig {
        TODO("Not yet implemented")
    }
}
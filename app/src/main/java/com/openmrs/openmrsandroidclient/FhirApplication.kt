package com.openmrs.openmrsandroidclient

import android.app.Application
import com.google.android.fhir.datacapture.DataCaptureConfig

class FhirApplication: Application(), DataCaptureConfig.Provider {
    override fun getDataCaptureConfig(): DataCaptureConfig {
        TODO("Not yet implemented")
    }
}
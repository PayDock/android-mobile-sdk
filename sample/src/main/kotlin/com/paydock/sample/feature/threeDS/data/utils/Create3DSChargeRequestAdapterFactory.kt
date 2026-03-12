package com.paydock.sample.feature.threeDS.data.utils

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.paydock.sample.feature.threeDS.data.api.dto.Capture3DSChargeRequest

class Create3DSChargeRequestAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val rawType = type.rawType
        if (rawType != Capture3DSChargeRequest::class.java) {
            return null
        }
        val mpgs3dsChargeAdapter = gson.getDelegateAdapter(
            this,
            TypeToken.get(Capture3DSChargeRequest.CaptureMPGS3dsChargeRequest::class.java)
        )
        val standalone3DSChargeAdapter = gson.getDelegateAdapter(
            this,
            TypeToken.get(Capture3DSChargeRequest.CaptureStandalone3DSChargeRequest::class.java)
        )
        return Create3DSChargeRequestAdapter(
            mpgs3dsChargeAdapter,
            standalone3DSChargeAdapter
        ) as TypeAdapter<T>
    }

    private class Create3DSChargeRequestAdapter(
        private val mpgs3dsChargeAdapter: TypeAdapter<Capture3DSChargeRequest.CaptureMPGS3dsChargeRequest>,
        private val standalone3DSChargeAdapter: TypeAdapter<Capture3DSChargeRequest.CaptureStandalone3DSChargeRequest>,
    ) : TypeAdapter<Capture3DSChargeRequest>() {

        override fun write(out: JsonWriter, value: Capture3DSChargeRequest) {
            when (value) {
                is Capture3DSChargeRequest.CaptureMPGS3dsChargeRequest -> mpgs3dsChargeAdapter.write(
                    out,
                    value
                )

                is Capture3DSChargeRequest.CaptureStandalone3DSChargeRequest -> standalone3DSChargeAdapter.write(
                    out,
                    value
                )
            }
        }

        override fun read(`in`: JsonReader): Capture3DSChargeRequest {
            throw UnsupportedOperationException("Deserialization not supported")
        }
    }
}
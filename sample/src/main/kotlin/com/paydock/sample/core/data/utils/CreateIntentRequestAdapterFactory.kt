package com.paydock.sample.core.data.utils

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.paydock.sample.feature.checkout.data.api.dto.CreateIntentRequest

class CreateIntentRequestAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val rawType = type.rawType
        if (rawType != CreateIntentRequest::class.java) {
            return null
        }
        val templateAdapter = gson.getDelegateAdapter(
            this,
            TypeToken.get(CreateIntentRequest.TemplateIntentRequest::class.java)
        )
        val directAdapter = gson.getDelegateAdapter(
            this,
            TypeToken.get(CreateIntentRequest.DirectIntentRequest::class.java)
        )
        return CreateIntentRequestAdapter(templateAdapter, directAdapter) as TypeAdapter<T>
    }

    private class CreateIntentRequestAdapter(
        private val templateAdapter: TypeAdapter<CreateIntentRequest.TemplateIntentRequest>,
        private val directAdapter: TypeAdapter<CreateIntentRequest.DirectIntentRequest>
    ) : TypeAdapter<CreateIntentRequest>() {

        override fun write(out: JsonWriter, value: CreateIntentRequest) {
            when (value) {
                is CreateIntentRequest.TemplateIntentRequest -> templateAdapter.write(out, value)
                is CreateIntentRequest.DirectIntentRequest -> directAdapter.write(out, value)
            }
        }

        override fun read(`in`: JsonReader): CreateIntentRequest {
            throw UnsupportedOperationException("Deserialization not supported")
        }
    }
}
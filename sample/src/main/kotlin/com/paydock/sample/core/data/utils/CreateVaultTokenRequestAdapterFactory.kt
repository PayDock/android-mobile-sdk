package com.paydock.sample.core.data.utils

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.paydock.sample.feature.card.data.api.dto.VaultTokenRequest

class CreateVaultTokenRequestAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val rawType = type.rawType
        if (rawType != VaultTokenRequest::class.java) {
            return null
        }
        val cardVaultTokenAdapter = gson.getDelegateAdapter(
            this,
            TypeToken.get(VaultTokenRequest.CreateCardVaultTokenRequest::class.java)
        )
        val cardSessionTokenAdapter = gson.getDelegateAdapter(
            this,
            TypeToken.get(VaultTokenRequest.CreateCardSessionVaultTokenRequest::class.java)
        )
        return CreateVaultTokenRequestAdapter(
            cardVaultTokenAdapter,
            cardSessionTokenAdapter
        ) as TypeAdapter<T>
    }

    private class CreateVaultTokenRequestAdapter(
        private val cardVaultTokenAdapter: TypeAdapter<VaultTokenRequest.CreateCardVaultTokenRequest>,
        private val cardSessionTokenAdapter: TypeAdapter<VaultTokenRequest.CreateCardSessionVaultTokenRequest>,
    ) : TypeAdapter<VaultTokenRequest>() {

        override fun write(out: JsonWriter, value: VaultTokenRequest) {
            when (value) {
                is VaultTokenRequest.CreateCardVaultTokenRequest -> cardVaultTokenAdapter.write(
                    out,
                    value
                )

                is VaultTokenRequest.CreateCardSessionVaultTokenRequest -> cardSessionTokenAdapter.write(
                    out,
                    value
                )
            }
        }

        override fun read(`in`: JsonReader): VaultTokenRequest {
            throw UnsupportedOperationException("Deserialization not supported")
        }
    }
}
package com.paydock.sample.feature.widgets.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.feature.address.presentation.AddressDetailsWidget

@Composable
fun AddressDetailsItem(context: Context) {
    AddressDetailsWidget(modifier = Modifier.padding(16.dp)) { result ->
        Toast.makeText(context, "Address details returned [$result]", Toast.LENGTH_SHORT).show()
    }
}
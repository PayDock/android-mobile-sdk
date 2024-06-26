package com.paydock.sample.feature.checkout.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paydock.sample.designsystems.theme.Theme

@Composable
fun BasketItemView(
    title: String,
    description: String,
    price: String,
    @DrawableRes image: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Theme.colors.primary.copy(alpha = 0.4f), shape = RoundedCornerShape(10.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .background(Theme.colors.primaryContainer, shape = RoundedCornerShape(10.dp))
                .padding(16.dp),
            painter = painterResource(image),
            contentDescription = title
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = Theme.typography.body)
            Text(text = description, style = Theme.typography.label)
            Text(text = price, style = Theme.typography.body, fontWeight = FontWeight.Bold)
        }
    }
}
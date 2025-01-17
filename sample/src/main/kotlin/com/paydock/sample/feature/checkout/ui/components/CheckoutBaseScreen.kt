package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.sample.R
import com.paydock.sample.core.AMOUNT
import com.paydock.sample.core.AU_COUNTRY_CODE
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.designsystems.theme.Theme
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun CheckoutBaseScreen(onCheckoutButtonClick: () -> Unit) {
    val productAmount = BigDecimal(AMOUNT).toDouble().div(2)
    val subtotal = productAmount * 2
    val formattedSubtotal = formatCurrency(subtotal)
    val formattedProductAmount = formatCurrency(productAmount)

    Column {
        HorizontalDivider(color = Theme.colors.outlineVariant)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Your cart",
                fontSize = 20.sp,
            )
            BasketItemView(
                title = "ThinkPad X1 Yoga Gen 7",
                description = "ThinkPad X1 Yoga Gen 7\n14” Intel 2 in 1 Laptop",
                price = formattedProductAmount,
                image = R.drawable.demo_product_1
            )
            BasketItemView(
                title = "Galaxy S23 Ultra",
                description = "SM-S918BZGHSEK\n512 GB｜12 GB｜Green",
                price = formattedProductAmount,
                image = R.drawable.demo_product_2
            )
            Spacer(modifier = Modifier.weight(1.0f))
            TotalRowView(
                title = "Subtotal",
                value = formattedSubtotal,
                color = Theme.colors.outlineVariant
            )
            TotalRowView(title = "Shipping", value = "Free", color = Theme.colors.outlineVariant)
            HorizontalDivider(color = Theme.colors.outlineVariant)
            TotalRowView(
                title = "Total",
                value = formattedSubtotal,
                color = Theme.colors.onBackground
            )
            AppButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Checkout",
                onClick = onCheckoutButtonClick
            )
        }
    }
}

fun formatCurrency(amount: Double, locale: Locale = Locale("en", AU_COUNTRY_CODE)): String {
    val currencyFormatter = NumberFormat.getCurrencyInstance(locale)
    currencyFormatter.currency = Currency.getInstance(AU_CURRENCY_CODE)
    return currencyFormatter.format(amount)
}


package com.paydock.sample.designsystems.components.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.containers.TitleSection
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.checkout.models.CheckoutType

@Composable
fun ListRowItem(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    iconResource: Int? = null,
    onClicked: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClicked.invoke() }
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        iconResource?.let { iconRes ->
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
            Spacer(Modifier.width(16.dp))
        }
        TitleSection(modifier = Modifier.weight(1f), title = title, subTitle = description)
        Icon(
            painter = painterResource(R.drawable.ic_caret_right),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Preview(showBackground = true)
//@PreviewLightDark
@Composable
internal fun PreviewListRowItem() {
    SampleTheme {
        ListRowItem(
            title = CheckoutType.STANDALONE.displayName(),
            description = CheckoutType.STANDALONE.displayDescription(),
            iconResource = R.drawable.ic_address_widget
        ) {
        }
    }
}

@Preview(showBackground = true)
//@PreviewLightDark
@Composable
internal fun PreviewListRowItemWithoutSubtitle() {
    SampleTheme {
        ListRowItem(
            title = CheckoutType.STANDALONE.displayName(),
            description = null,
            iconResource = R.drawable.ic_address_widget
        ) {
        }
    }
}
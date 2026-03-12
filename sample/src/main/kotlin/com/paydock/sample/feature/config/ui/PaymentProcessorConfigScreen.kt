package com.paydock.sample.feature.config.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.EnumDropdown
import com.paydock.sample.designsystems.components.fields.TextField
import com.paydock.sample.designsystems.components.list.ExpandableListItem
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.config.models.PaymentProcessor
import com.paydock.sample.feature.config.models.ThreeDSService

@Composable
fun PaymentProcessorConfigScreen(
    viewModel: ConfigViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val checkoutConfig by viewModel.checkoutConfig.collectAsState()

    // Determine selection state
    val isMpgsSelected = checkoutConfig.preferredProcessor == PaymentProcessor.MPGS
    val isCyberSourceSelected = checkoutConfig.preferredProcessor == PaymentProcessor.CYBERSOURCE

    // State for expandable sections - expand the selected one by default
    var mpgsExpanded by remember(checkoutConfig.preferredProcessor) {
        mutableStateOf(isMpgsSelected)
    }
    var cyberSourceExpanded by remember(checkoutConfig.preferredProcessor) {
        mutableStateOf(isCyberSourceSelected)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Payment Processor Configuration",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Configure payment processor preferences and settings.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // MPGS Option
        ExpandableListItem(
            title = "MPGS",
            description = "Mastercard Payment Gateway Services",
            iconResource = R.drawable.ic_config,
            isExpanded = mpgsExpanded,
            isSelected = isMpgsSelected,
            onExpandedChange = { expanded ->
                mpgsExpanded = expanded
                // Collapse CyberSource when MPGS is expanded
                if (expanded) {
                    cyberSourceExpanded = false
                }
            },
            onSelectionChange = {
                // Select MPGS (one must always be selected)
                if (!isMpgsSelected) {
                    viewModel.updatePreferredProcessor(PaymentProcessor.MPGS)
                    mpgsExpanded = true
                    cyberSourceExpanded = false
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                TextField(
                    label = stringResource(R.string.label_service_id),
                    value = checkoutConfig.mpgsConfig.serviceId,
                    onValueChange = { viewModel.updateMpgsServiceId(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                EnumDropdown(
                    label = "3DS Service",
                    options = ThreeDSService.entries,
                    selectedOption = checkoutConfig.mpgsConfig.threeDSService,
                    onOptionSelected = { viewModel.updateMpgsThreeDSService(it) },
                    displayText = { service ->
                        when (service) {
                            ThreeDSService.MPGS_3DS -> "MPGS 3DS"
                            ThreeDSService.GPAYMENTS -> "GPayments"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // CyberSource Option
        ExpandableListItem(
            title = "CyberSource",
            description = "CyberSource payment processor",
            iconResource = R.drawable.ic_config,
            isExpanded = cyberSourceExpanded,
            isSelected = isCyberSourceSelected,
            onExpandedChange = { expanded ->
                cyberSourceExpanded = expanded
                // Collapse MPGS when CyberSource is expanded
                if (expanded) {
                    mpgsExpanded = false
                }
            },
            onSelectionChange = {
                // Select CyberSource (one must always be selected)
                if (!isCyberSourceSelected) {
                    viewModel.updatePreferredProcessor(PaymentProcessor.CYBERSOURCE)
                    cyberSourceExpanded = true
                    mpgsExpanded = false
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                TextField(
                    label = stringResource(R.string.label_service_id),
                    value = checkoutConfig.cyberSourceConfig.serviceId,
                    onValueChange = { viewModel.updateCyberSourceServiceId(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                EnumDropdown(
                    label = "3DS Service",
                    options = listOf(ThreeDSService.GPAYMENTS), // CyberSource only supports GPayments
                    selectedOption = checkoutConfig.cyberSourceConfig.threeDSService,
                    onOptionSelected = { viewModel.updateCyberSourceThreeDSService(it) },
                    displayText = { service ->
                        when (service) {
                            ThreeDSService.MPGS_3DS -> "MPGS 3DS"
                            ThreeDSService.GPAYMENTS -> "GPayments"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}


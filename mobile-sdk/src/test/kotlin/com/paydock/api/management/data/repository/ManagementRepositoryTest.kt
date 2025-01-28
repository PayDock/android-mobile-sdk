package com.paydock.api.management.data.repository

import com.paydock.api.management.data.dto.CardSchemasResponse
import com.paydock.api.management.data.mapper.asEntity
import com.paydock.api.management.domain.repository.ManagementRepository
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.injection.modules.mockFailureNetworkModule
import com.paydock.core.data.injection.modules.mockSuccessNetworkModule
import com.paydock.core.network.extensions.convertToDataClass
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.core.component.get
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.test.inject
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ManagementRepositoryTest : BaseKoinUnitTest() {

    private val httpMockClient: HttpClient by inject()

    private val testScope: TestScope by inject()
    private lateinit var repository: ManagementRepository

    @Test
    fun `GIVEN valid access token WHEN fetching card schemas THEN should succeed with card schemas response list`() =
        testScope.runTest {
            // GIVEN
            repository = ManagementRepositoryImpl(get(), httpMockClient)

            val response =
                readResourceFile("management/success_get_card_schemas_response.json")
                    .convertToDataClass<CardSchemasResponse>()
            val entity = response.asEntity()
            // WHEN - Call the method to be tested
            val result = repository.getCardSchemas(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN
            )
            // THEN - Verify the result
            assertNotNull(response)
            assertEquals(entity, result)
        }

    @Test(expected = ClientRequestException::class)
    fun `GIVEN invalid access token WHEN fetching card schemas THEN should fail with error response resource`() =
        testScope.runTest {
            val invalidAccessToken = MobileSDKTestConstants.Wallet.MOCK_INVALID_WALLET_TOKEN

            unloadKoinModules(mockSuccessNetworkModule)
            loadKoinModules(mockFailureNetworkModule)
            // GIVEN
            repository = ManagementRepositoryImpl(get(), httpMockClient)
            // WHEN - Call the method to be tested
            val result = repository.getCardSchemas(invalidAccessToken)
            // THEN - It should throw an exception
            assertNotNull(result)
        }

}
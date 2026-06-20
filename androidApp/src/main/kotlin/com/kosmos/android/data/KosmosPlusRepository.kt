package com.kosmos.android.data

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KosmosPlusRepository(
    private val context: Context,
    private val prefs: PreferencesRepository,
    private val scope: CoroutineScope,
) : PurchasesUpdatedListener {

    private val _isPlus = MutableStateFlow(false)
    val isPlus: StateFlow<Boolean> = _isPlus.asStateFlow()

    private var billingClient: BillingClient? = null
    private var productDetails: ProductDetails? = null

    suspend fun refreshStatus() {
        _isPlus.value = prefs.isKosmosPlusActive()
    }

    fun connect(activity: Activity) {
        if (billingClient?.isReady == true) return
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProduct()
                }
            }

            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun queryProduct() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                ),
            )
            .build()
        billingClient?.queryProductDetailsAsync(params) { result, list ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetails = list.firstOrNull()
            }
        }
    }

    fun launchPurchase(activity: Activity) {
        val details = productDetails
        if (details == null) {
            activateDebugPurchase()
            return
        }
        val offer = details.subscriptionOfferDetails?.firstOrNull() ?: run {
            activateDebugPurchase()
            return
        }
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offer.offerToken)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        billingClient?.launchBillingFlow(activity, flowParams)
    }

    fun activateDebugPurchase() {
        scope.launch(Dispatchers.IO) {
            prefs.setKosmosPlus(true)
            _isPlus.value = true
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode != BillingClient.BillingResponseCode.OK || purchases == null) return
        for (purchase in purchases) {
            if (purchase.products.contains(PRODUCT_ID)) {
                handlePurchase(purchase)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            scope.launch(Dispatchers.IO) {
                prefs.setKosmosPlus(true)
                _isPlus.value = true
            }
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient?.acknowledgePurchase(params) { }
            }
        }
    }

    companion object {
        const val PRODUCT_ID = "kosmos_plus_monthly"
    }
}

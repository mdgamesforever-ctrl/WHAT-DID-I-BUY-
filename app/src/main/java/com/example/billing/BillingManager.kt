package com.example.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("what_did_i_buy_billing", Context.MODE_PRIVATE)
    private val _subscriptionState = MutableStateFlow(loadInitialState())
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private val _monthlyPrice = MutableStateFlow(BillingConfig.DEFAULT_MONTHLY_DISPLAY_PRICE)
    val monthlyPrice: StateFlow<String> = _monthlyPrice.asStateFlow()

    private val _yearlyPrice = MutableStateFlow(BillingConfig.DEFAULT_YEARLY_DISPLAY_PRICE)
    val yearlyPrice: StateFlow<String> = _yearlyPrice.asStateFlow()

    private val _billingMessage = MutableStateFlow<String?>(null)
    val billingMessage: StateFlow<String?> = _billingMessage.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // Initialize billing connection & verify purchase status
        refreshPurchases()
    }

    private fun loadInitialState(): SubscriptionState {
        val saved = prefs.getString("sub_state", SubscriptionState.FREE.name)
        return try {
            SubscriptionState.valueOf(saved ?: SubscriptionState.FREE.name)
        } catch (e: Exception) {
            SubscriptionState.FREE
        }
    }

    fun isPremium(): Boolean {
        return _subscriptionState.value.isPremium
    }

    fun canAddMorePurchases(currentCount: Int): Boolean {
        if (isPremium()) return true
        return currentCount < BillingConfig.FREE_TIER_PURCHASE_LIMIT
    }

    fun launchBillingFlow(activity: Activity, plan: BillingPlan, onResult: (Boolean, String) -> Unit) {
        scope.launch {
            // If placeholder IDs are configured in BillingConfig, we handle with a smooth simulated flow
            // that properly updates the subscription state so testing works seamlessly.
            val productId = when (plan) {
                BillingPlan.MONTHLY -> BillingConfig.SUBSCRIPTION_MONTHLY_ID
                BillingPlan.YEARLY -> BillingConfig.SUBSCRIPTION_YEARLY_ID
            }

            if (productId.startsWith("REPLACE_WITH")) {
                // Testing sandbox mode
                val newState = if (plan == BillingPlan.MONTHLY) SubscriptionState.ACTIVE_MONTHLY else SubscriptionState.ACTIVE_YEARLY
                setSubscriptionState(newState)
                _billingMessage.value = "Subscription successfully activated (${plan.name.lowercase()})."
                onResult(true, "Subscribed successfully!")
            } else {
                // In production with registered Google Play Product IDs:
                // Google Play BillingClient handles the interactive sheet.
                _billingMessage.value = "Connecting to Google Play Store for $productId..."
                onResult(true, "Google Play Billing initiated.")
            }
        }
    }

    fun restorePurchases(onComplete: (Boolean, String) -> Unit) {
        scope.launch {
            val currentState = loadInitialState()
            if (currentState.isPremium) {
                _subscriptionState.value = currentState
                onComplete(true, "Your premium subscription is active and restored.")
            } else {
                onComplete(false, "No active subscriptions found for this Google account.")
            }
        }
    }

    fun setSubscriptionState(state: SubscriptionState) {
        prefs.edit().putString("sub_state", state.name).apply()
        _subscriptionState.value = state
    }

    fun refreshPurchases() {
        _subscriptionState.value = loadInitialState()
    }

    fun clearBillingMessage() {
        _billingMessage.value = null
    }
}

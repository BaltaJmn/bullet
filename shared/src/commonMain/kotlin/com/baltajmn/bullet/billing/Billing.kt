package com.baltajmn.bullet.billing

import com.baltajmn.bullet.data.BobbinRepository
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException

/**
 * The public SDK key of the RevenueCat project, one per store. Null until the project exists, and
 * the app then runs as free only instead of crashing.
 */
expect val revenueCatApiKey: String?

/**
 * The single paid product: a non-consumable that grants [ENTITLEMENT] forever. The product id and
 * the price live in the RevenueCat dashboard and never here, so changing the price is not an app
 * update (docs/tecnico.md 6.16, store/revenuecat.md).
 */
object Billing {

    const val ENTITLEMENT = "pro"

    private var configured = false

    /** Called once at startup. With no key it does nothing at all. */
    fun configure() {
        if (configured) return
        val key = revenueCatApiKey ?: return
        configured = true
        Purchases.logLevel = LogLevel.WARN
        Purchases.configure(PurchasesConfiguration.Builder(key).build())
    }

    /**
     * Re-checks the entitlement against the store. A failure leaves the cached value alone on
     * purpose: whoever paid keeps their Pro on a plane.
     */
    suspend fun refresh() {
        if (!configured) return
        runCatching { Purchases.sharedInstance.awaitCustomerInfo() }
            .onSuccess { BobbinRepository.updatePro(it.entitlements[ENTITLEMENT]?.isActive == true) }
    }

    /** What to sell, or null while offline or before the dashboard is filled in. */
    suspend fun proPackage(): Package? {
        if (!configured) return null
        return runCatching {
            Purchases.sharedInstance.awaitOfferings().current?.availablePackages?.firstOrNull()
        }.getOrNull()
    }

    suspend fun purchase(pack: Package): PurchaseOutcome = try {
        val purchase = Purchases.sharedInstance.awaitPurchase(packageToPurchase = pack)
        BobbinRepository.updatePro(purchase.customerInfo.entitlements[ENTITLEMENT]?.isActive == true)
        PurchaseOutcome.Success
    } catch (e: PurchasesTransactionException) {
        if (e.userCancelled) PurchaseOutcome.Cancelled else PurchaseOutcome.Failed
    } catch (e: Exception) {
        PurchaseOutcome.Failed
    }

    /** Both stores require this to be reachable without buying anything first. */
    suspend fun restore(): Boolean {
        if (!configured) return false
        val info = runCatching { Purchases.sharedInstance.awaitRestore() }.getOrNull() ?: return false
        val active = info.entitlements[ENTITLEMENT]?.isActive == true
        BobbinRepository.updatePro(active)
        return active
    }
}

enum class PurchaseOutcome { Success, Cancelled, Failed }

package com.revanthdev.expensetrackr.ads

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.revanthdev.expensetrackr.BuildConfig
import com.revanthdev.expensetrackr.core.database.dao.AdOperationCounterDao
import com.revanthdev.expensetrackr.core.database.entity.AdOperationCounterEntity
import com.revanthdev.expensetrackr.core.presentation.RewardedActionGate
import kotlinx.coroutines.launch

/** Requires a rewarded ad on operation 3, 6, 9… and persists progress in Room. */
class AndroidRewardedActionGate(
    private val activity: FragmentActivity,
    private val dao: AdOperationCounterDao,
) : RewardedActionGate {
    private var requestInFlight = false

    override fun run(operation: String, action: () -> Unit) {
        if (requestInFlight) return
        activity.lifecycleScope.launch {
            val next = (dao.count(operation) ?: 0) + 1
            if (next % 3 != 0) {
                dao.save(AdOperationCounterEntity(operation, next))
                action()
                return@launch
            }
            requestInFlight = true
            RewardedAd.load(
                activity,
                BuildConfig.ADMOB_REWARDED_ID,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        requestInFlight = false
                        activity.lifecycleScope.launch {
                            dao.save(AdOperationCounterEntity(operation, next))
                            action()
                        }
                    }

                    override fun onAdLoaded(ad: RewardedAd) {
                        var earned = false
                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                requestInFlight = false
                            }

                            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                                requestInFlight = false
                                activity.lifecycleScope.launch {
                                    dao.save(AdOperationCounterEntity(operation, next))
                                    action()
                                }
                            }
                        }
                        ad.show(activity) {
                            if (earned) return@show
                            earned = true
                            activity.lifecycleScope.launch {
                                dao.save(AdOperationCounterEntity(operation, next))
                                action()
                            }
                        }
                    }
                },
            )
        }
    }
}

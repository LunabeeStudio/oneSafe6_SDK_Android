package studio.lunabee.onesafe.feature.home

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface ReviewAppDelegate {
    fun showFeedbackDialog(activity: Context)
}

class ReviewAppDelegateImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : ReviewAppDelegate {

    override fun showFeedbackDialog(activity: Context) {
        val reviewManager = ReviewManagerFactory.create(appContext)

        reviewManager.requestReviewFlow().addOnCompleteListener {
            if (it.isSuccessful) {
                reviewManager.launchReviewFlow(activity as Activity, it.result)
            }
        }
    }
}

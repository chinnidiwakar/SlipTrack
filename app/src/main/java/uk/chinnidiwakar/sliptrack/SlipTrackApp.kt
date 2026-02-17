package uk.chinnidiwakar.sliptrack

import android.app.Application

class SlipTrackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MilestoneWorkScheduler.schedule(this)
    }
}

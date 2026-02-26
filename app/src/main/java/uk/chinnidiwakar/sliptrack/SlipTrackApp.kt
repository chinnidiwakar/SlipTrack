package uk.chinnidiwakar.sliptrack

import android.app.Application

class SlipTrackApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        MilestoneWorkScheduler.schedule(this)
    }

    companion object {
        lateinit var instance: SlipTrackApp
            private set
    }
}

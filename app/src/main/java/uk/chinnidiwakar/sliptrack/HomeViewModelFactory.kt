package uk.chinnidiwakar.sliptrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HomeViewModelFactory(
    private val dao: SlipDao,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            // Use the 'dao' and 'preferenceManager' passed into the constructor
            return HomeViewModel(dao, preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
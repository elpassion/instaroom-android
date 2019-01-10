package pl.elpassion.instaroom.util

import androidx.lifecycle.MutableLiveData

@Suppress("NOTHING_TO_INLINE")
inline fun <State> MutableLiveData<State>.set(state: State) = postValue(state)

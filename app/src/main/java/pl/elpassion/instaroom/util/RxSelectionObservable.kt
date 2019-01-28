package pl.elpassion.instaroom.util

import androidx.annotation.CheckResult
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.Tab
import com.jakewharton.rxbinding3.internal.checkMainThread
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

@CheckResult
fun TabLayout.selections(): Observable<Tab> {
    return TabLayoutSelectionsObservable(this)
}

private class TabLayoutSelectionsObservable(
    private val view: TabLayout
) : Observable<Tab>() {

    override fun subscribeActual(observer: Observer<in Tab>) {
//        if (!checkMainThread(observer)) {
//            return
//        }
        val listener = Listener(view,
            observer)
        observer.onSubscribe(listener)
        view.addOnTabSelectedListener(listener)
        val index = view.selectedTabPosition
        if (index != -1) {
            observer.onNext(view.getTabAt(index)!!)
        }
    }

    private class Listener(
        private val tabLayout: TabLayout,
        private val observer: Observer<in Tab>
    ) : MainThreadDisposable(),
        TabLayout.BaseOnTabSelectedListener<Tab> {


        override fun onDispose() {
            tabLayout.removeOnTabSelectedListener(this)
        }

        override fun onTabSelected(tab: Tab) {
            if (!isDisposed) {
                observer.onNext(tab)
            }
        }

        override fun onTabUnselected(tab: Tab) {}

        override fun onTabReselected(tab: Tab) {}
    }
}
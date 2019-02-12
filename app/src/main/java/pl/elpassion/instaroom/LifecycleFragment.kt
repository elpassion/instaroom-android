package pl.elpassion.instaroom

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.koin.android.viewmodel.ext.android.sharedViewModel

abstract class LifecycleFragment : Fragment() {

    protected val model by sharedViewModel<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(model)
    }
}
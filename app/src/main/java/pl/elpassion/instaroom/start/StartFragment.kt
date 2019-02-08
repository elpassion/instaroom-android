package pl.elpassion.instaroom.start

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.koin.android.viewmodel.ext.android.getSharedViewModel
import pl.elpassion.instaroom.AppViewModel

class StartFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSharedViewModel<AppViewModel>()
    }
}

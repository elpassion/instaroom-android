package pl.elpassion.instaroom.start

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.shopify.livedataktx.first
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import org.koin.android.viewmodel.ext.android.sharedViewModel
import pl.elpassion.instaroom.AppViewModel
import pl.elpassion.instaroom.R

class StartFragment : Fragment() {

    private val model by sharedViewModel<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model.loginState.nonNull().first().observe { state ->
            findNavController().navigate(
                if (state.isSignedIn) {
                    R.id.action_startFragment_to_dashboardFragment
                } else {
                    R.id.action_startFragment_to_loginFragment
                }
            )
        }
    }
}

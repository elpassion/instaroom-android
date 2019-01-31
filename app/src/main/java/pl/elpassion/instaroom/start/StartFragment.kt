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
        model.toString() // TODO: do anything to create model -> what to do?
    }
}

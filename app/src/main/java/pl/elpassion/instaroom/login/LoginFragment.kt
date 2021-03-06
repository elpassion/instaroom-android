package pl.elpassion.instaroom.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.android.synthetic.main.login_fragment.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import pl.elpassion.instaroom.AppViewModel
import pl.elpassion.instaroom.R

class LoginFragment : Fragment() {

    private val model by sharedViewModel<AppViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.login_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .requestScopes(Scope("profile"), Scope("https://www.googleapis.com/auth/calendar.events"))
            .build()

        val googleSignInClient = GoogleSignIn.getClient(activity!!, googleSignInOptions)

        signInButton.setOnClickListener {
            startActivityForResult(
                googleSignInClient.signInIntent,
                SIGN_IN_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            userSignedIn()
        }
    }

    private fun userSignedIn() {
        model.loginActionS.accept(LoginAction.UserSignedIn)
    }


    companion object {
        const val SIGN_IN_REQUEST_CODE = 627
    }
}

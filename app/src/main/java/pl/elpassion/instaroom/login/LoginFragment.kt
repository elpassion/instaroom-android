package pl.elpassion.instaroom.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.login_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

        authWithGoogleButton.setOnClickListener {
            startActivityForResult(
                googleSignInClient.signInIntent,
                SIGN_IN_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val result = task.getResult(ApiException::class.java)
            GlobalScope.launch(Dispatchers.IO) {
                val token = GoogleAuthUtil.getToken(
                    activity!!,
                    result!!.account,
                    "oauth2:https://www.googleapis.com/auth/calendar.events"
                )
                model.loginActionS.accept(LoginAction.SaveGoogleToken(token))
                findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val SIGN_IN_REQUEST_CODE = 627
    }
}

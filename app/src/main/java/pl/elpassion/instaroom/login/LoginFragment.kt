package pl.elpassion.instaroom.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.android.synthetic.main.login_fragment.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import pl.elpassion.instaroom.AppViewModel
import pl.elpassion.instaroom.LifecycleFragment
import pl.elpassion.instaroom.R
import android.widget.TextView



class LoginFragment : LifecycleFragment() {

    private val googleSignInClient: GoogleSignInClient by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.login_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.loginInfoD.observe(this, Observer(::showInfo))

        signInButton.setOnClickListener {
            startActivityForResult(
                googleSignInClient.signInIntent,
                SIGN_IN_REQUEST_CODE
            )
        }
    }

    private fun showInfo(loginInfo: LoginInfo?) {
        loginInfo?.message?.run {
            val toast = Toast.makeText(context, this, Toast.LENGTH_LONG)
            val tv: View? = toast.view.findViewById(android.R.id.message)
            tv?.let {
                if(tv is TextView) {
                    tv.gravity = Gravity.CENTER
                }
            }
            toast.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            userSignedIn()
        }
    }

    private fun userSignedIn() {
        model.loginActionS.accept(SignInAction)
    }


    companion object {
        const val SIGN_IN_REQUEST_CODE = 627
    }
}

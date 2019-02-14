package pl.elpassion.instaroom.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.android.synthetic.main.login_fragment.*
import org.koin.android.ext.android.inject
import pl.elpassion.instaroom.LifecycleFragment
import pl.elpassion.instaroom.R
import android.widget.TextView
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable


class LoginFragment : LifecycleFragment() {

    private val googleSignInClient: GoogleSignInClient by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.login_fragment, container, false)

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.loginInfoD.observe(this, Observer(::showInfo))

        setShowPrivacyPolicyClick()

        Observable.mergeArray(
            setShowPrivacyPolicyClick(),
            setHidePrivacyPolicyClick()
        ).subscribe(model.loginActionS)

        signInButton.setOnClickListener {
            startActivityForResult(
                googleSignInClient.signInIntent,
                SIGN_IN_REQUEST_CODE
            )
        }
    }

    private fun setHidePrivacyPolicyClick() =
        closePrivacyImageView.clicks()
            .map { LoginAction.HidePrivacyPolicy }

    private fun setShowPrivacyPolicyClick() =
        privacyPolicyButton.clicks()
            .map { LoginAction.SelectPrivacyPolicy }

    private fun showInfo(loginInfo: LoginInfo?) {
        loginInfo?: return
        when (loginInfo) {
            is LoginInfo.Message -> showToast(loginInfo.message)
            is LoginInfo.PrivacyHtml -> showPrivacyPolicy()
            is LoginInfo.Default -> hidePrivacyPolicy()
        }
    }

    private fun hidePrivacyPolicy() {
        privacyGroup.visibility = View.GONE
        loginGroup.visibility = View.VISIBLE
    }

    private fun showPrivacyPolicy() {
        privacyWebView.loadUrl(PRIVACY_POLICY_FILE)
        privacyGroup.visibility = View.VISIBLE
        loginGroup.visibility = View.GONE
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        val tv: View? = toast.view.findViewById(android.R.id.message)
        tv?.let {
            if (tv is TextView) {
                tv.gravity = Gravity.CENTER
            }
        }
        toast.show()    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            userSignedIn()
        }
    }

    private fun userSignedIn() {
        model.loginActionS.accept(LoginAction.SignInAction)
    }


    companion object {
        const val SIGN_IN_REQUEST_CODE = 627

        const val PRIVACY_POLICY_FILE = "file:///android_asset/privacy_policy.html"
    }
}

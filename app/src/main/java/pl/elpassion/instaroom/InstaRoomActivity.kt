package pl.elpassion.instaroom

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import org.koin.android.ext.android.inject

class InstaRoomActivity : AppCompatActivity() {

    private val navHostFragment:NavHostFragment by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.instaroom_activity)

        if(savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.navigationContainer, navHostFragment)
                .setPrimaryNavigationFragment(navHostFragment)
                .commit()
        }
    }
}

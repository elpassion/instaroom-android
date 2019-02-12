package pl.elpassion.instaroom

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.markodevcic.peko.Peko
import io.reactivex.Observable
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.consumeEach

suspend fun runPermissionFlow(
    lifecycleActionS: Observable<LifecycleAction>,
    permissionList: List<String>
) : Boolean {

    val source = lifecycleActionS.firstOrError().await().source

    println("source = $source")
    val sourceActivity: AppCompatActivity

    sourceActivity = if(source is Fragment) {
        source.context as AppCompatActivity
    } else {
        source as AppCompatActivity
    }

    val (grantedPermissions) = Peko.requestPermissionsAsync(sourceActivity, *permissionList.toTypedArray())
    return grantedPermissions == permissionList
}
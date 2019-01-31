package pl.elpassion.instaroom.dashboard

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyBlocking
import io.kotlintest.specs.FreeSpec
import kotlinx.coroutines.GlobalScope

class DashboardModelTest : FreeSpec() {

    init {
        val getToken = mock<suspend () -> String?>()
        GlobalScope.runDashboardFlow(
            mock(),
            mock(),
            mock(),
            mock(),
            getToken
        )
        verifyBlocking(getToken) { invoke() }
    }
}

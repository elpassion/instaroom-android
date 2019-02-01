package pl.elpassion.instaroom.summary

import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay2.PublishRelay
import io.kotlintest.IsolationMode
import io.kotlintest.specs.FreeSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import org.junit.Assert.*
import pl.elpassion.instaroom.util.clearTaskExecutorDelegate
import pl.elpassion.instaroom.util.executeTasksInstantly
import kotlin.coroutines.CoroutineContext

class SummaryModelTest : FreeSpec(), CoroutineScope {
    @ExperimentalCoroutinesApi
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Unconfined + job

    override fun isolationMode(): IsolationMode? {
        return IsolationMode.InstancePerLeaf
    }

    private val job = Job()
    private val actionS = PublishRelay.create<SummaryAction>()
    private val state = MutableLiveData<SummaryState>()

    init {
        executeTasksInstantly()


        job.cancel()
        clearTaskExecutorDelegate()
    }

}
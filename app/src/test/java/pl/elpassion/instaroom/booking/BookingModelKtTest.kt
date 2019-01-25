package pl.elpassion.instaroom.booking

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.junit.After
import org.junit.Before
import org.junit.Test
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.clearTaskExecutorDelegate
import pl.elpassion.instaroom.util.executeTasksInstantly
import kotlin.coroutines.CoroutineContext

class BookingModelTest : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Unconfined + job

    private val job = Job()
    private val actionS = PublishRelay.create<BookingAction>()
    private val state = MutableLiveData<BookingState>()
    private val stateObserver = mock<Observer<BookingState>>()

    @Before
    fun setup() {
        executeTasksInstantly()
        launchBookingModel(actionS, mock(), state, mock())
        state.observeForever(stateObserver)
    }

    @After
    fun teardown() {
        job.cancel()
        clearTaskExecutorDelegate()
    }

    @Test
    fun `show room on book clicked`() {
        val selectedRoom = createRoom()
        actionS.accept(BookingAction.BookClicked(selectedRoom))
        verify(stateObserver).onChanged(BookingState(selectedRoom, true))
    }

}

private fun createRoom() = Room("", "", emptyList(), "", "", "", "")

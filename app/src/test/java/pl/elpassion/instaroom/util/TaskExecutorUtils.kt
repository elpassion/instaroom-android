package pl.elpassion.instaroom.util

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor

fun executeTasksInstantly() = ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
    override fun executeOnDiskIO(runnable: Runnable) = runnable.run()

    override fun isMainThread(): Boolean = true

    override fun postToMainThread(runnable: Runnable) = runnable.run()

})

fun clearTaskExecutorDelegate() = ArchTaskExecutor.getInstance().setDelegate(null)
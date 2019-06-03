@file:Suppress("TestFunctionName")

package pl.elpassion.instaroom.registration

import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay2.PublishRelay
import com.jraska.livedata.test
import io.kotlintest.IsolationMode
import io.kotlintest.TestCaseOrder
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.awaitFirst
import pl.elpassion.instaroom.util.executeTasksInstantly
import pl.mareklangiewicz.smokk.smokk
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

class ExampleTestsForRegistrationFlow : FreeSpec(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Unconfined + job

    override fun isolationMode(): IsolationMode? = IsolationMode.InstancePerLeaf
    override fun testCaseOrder(): TestCaseOrder? = TestCaseOrder.Sequential


    private val job = Job()


    init {
        executeTasksInstantly()
        "On registration flow launch" - {
            val invalidLogin = "   "
            val invalidPassword = "  "
            val validToken = "abcd"
            val invalidToken = ""
            val validLogin = "login"
            val validPassword = "password"
            val state = MutableLiveData<RegistrationState>()
            val registerAction = PublishRelay.create<RegisterAction>()
            val tokenAction = PublishRelay.create<TokenAction>()
            val api = MockRegisterApi()
            val stateObserver = state.test()

            val flow = launch {
                RegistrationFlow(
                    state,
                    registerAction,
                    tokenAction,
                    api
                )
            }

            "user is not registered" {
                stateObserver.assertValue(RegistrationState.NOT_REGISTERED)
            }

            "on register action with invalid credentials" - {
                registerAction.accept(RegisterAction(invalidLogin, invalidPassword))

                "invalid credentials do not make a call" {
                    api.register.invocations shouldHaveSize 0
                }

                "on register action with corrected credentials" - {
                    registerAction.accept(RegisterAction(validLogin, validPassword))

                    "call api to register user" {
                        api.register.invocations shouldContain Pair(validLogin, validPassword)
                    }
                }
            }


            "on register action with valid credentials" - {
                registerAction.accept(RegisterAction(validLogin, validPassword))

                "call api to register user" {
                    api.register.invocations shouldContain Pair(validLogin, validPassword)
                }

                "on api call resulted with failure" - {
                    api.register.resume(RegisterResult.Failure)

                    "user is not registered"{
                        stateObserver.assertValue(RegistrationState.NOT_REGISTERED)
                    }

                    "do not complete flow" {
                        flow.isCompleted shouldBe false
                    }
                }


                "on api call resulted with success" - {
                    api.register.resume(RegisterResult.Success)

                    "on valid token returned" - {
                        tokenAction.accept(TokenAction(validToken))
                        api.proceedToken.resume(TokenResult.Valid)

                        "user is registered"{
                            stateObserver.assertValue(RegistrationState.REGISTERED)
                        }
                        "complete flow" {
                            flow.isCompleted shouldBe true
                        }
                    }
                }
            }
        }
    }
}


class MockRegisterApi : RegisterApi {

    val register = smokk<String, String, RegisterResult>()
    val proceedToken = smokk<String, TokenResult>()


    override suspend fun register(login: String, password: String): RegisterResult =
        register.invoke(login, password)

    override var accessToken: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    override fun getToken(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun proceedToken(token: String): TokenResult =
        proceedToken.invoke(token)
}

suspend fun RegistrationFlow(
    state: MutableLiveData<RegistrationState>,
    registerAction: Observable<RegisterAction>,
    tokenAction: Observable<TokenAction>,
    api: RegisterApi
) {
    state.value = RegistrationState.NOT_REGISTERED
    flow@ while (true) {
        val (login, password) = registerAction.awaitFirst()

        if (login.isBlank() or password.isBlank()) {
            continue@flow
        }
        val result = api.register(login, password)
        if (result == RegisterResult.Failure) {
            continue@flow
        }

        val (token) = tokenAction.awaitFirst()

        val tokenResult = api.proceedToken(token)

        if (tokenResult != TokenResult.Valid) {
            continue@flow
        }
        state.value = RegistrationState.REGISTERED
        break@flow
    }
}

data class RegisterAction(val login: String, val password: String)
data class TokenAction(val token: String)

enum class RegistrationState {
    NOT_REGISTERED,
    REGISTERED
}

interface RegisterApi {
    suspend fun register(login: String, password: String): RegisterResult

    var accessToken: String?

    fun getToken(): String

    suspend fun proceedToken(token: String): TokenResult
}

sealed class RegisterResult {
    object Success : RegisterResult()
    object Failure : RegisterResult()
}


sealed class TokenResult {
    object Valid : TokenResult()
    object Invalid : TokenResult()
}

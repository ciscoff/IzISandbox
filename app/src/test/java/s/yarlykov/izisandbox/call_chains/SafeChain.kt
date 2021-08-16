package s.yarlykov.izisandbox.call_chains

import org.junit.Test

/**
 * Как читать chain:
 * SafeChain(action).ifError(in_action).call(with_another_action)
 *
 * Первым выполнить action внутри ifError. Если там происходит error, то
 * создается новый SafeChain уже с новым action (который берется из after).
 * Если error не происходит, то SafeChain остается прежним. В любом случае
 * после следует вызов SafeChain.call() на прежнем или новом инстансе SafeChain.
 *
 * @property action
 */
class SafeChain(private val action: () -> Unit) {
    private var isSucceed = false

    fun ifError(after: () -> Unit): SafeChain {

        println("should be first: call ifError(...)")

        if (isSucceed) return this
        return try {
            action.invoke().run { this@SafeChain.apply { isSucceed = true } }
        } catch (_: Exception) {
            SafeChain(after)
        }
    }

    fun call() {
        println("should be second: call call(...)")

        if (isSucceed) return
        try {
            action.invoke()
            isSucceed = true
        } catch (_: Exception) {
        }
    }
}


class TestSafeChain() {

    @Test
    fun testChainCalls() {
        SafeChain {
            throw IllegalArgumentException("")
        }.ifError {
            println("should be last: Error was occurred")
        }.call()
    }
}
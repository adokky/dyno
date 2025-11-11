package dyno

import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

internal inline fun <reified E: Throwable> asssertFailsWithMessage(
    vararg fragments: String,
    body: () -> Unit
) {
    val msg = assertFailsWith<E> { body() }.message
    assertNotNull(msg)
    for (fragment in fragments) {
        assertContains(msg, fragment, ignoreCase = true)
    }
}
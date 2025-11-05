package dyno

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

abstract class AbstractClassMapTest {
    @Serializable
    @SerialName("Base") protected sealed class Base
    @Serializable
    @SerialName("A") protected data class A(val s: String): Base()
    @Serializable
    @SerialName("B") protected data class B(val i: Int): Base()
    @Serializable
    @SerialName("С") protected data class C(val s: String): Base()

    protected val a = A("foo")
    protected val b = B(565143)
    protected val с = C("bar")
}
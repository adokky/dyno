package dyno

sealed class AbstractDynoTest {
    protected val p1 = DynoKey<String?>("p1")
    protected val p2 = DynoKey<Int?>("p2")
    protected val p3 = DynoKey<List<Boolean>?>("p3")
    protected val pListOfInts = DynoKey<List<Int>?>("listOfInts")
    protected val pMapOfInts = DynoKey<Map<Int, Int>?>("mapOfInts")

    protected val e1 = SimpleDynoEntry(p1, "test")
    protected val e2 = SimpleDynoEntry(p2, 77)
    protected val e3 = SimpleDynoEntry(p3, listOf(true))

    protected val r1 = DynoKey<String>("r1")
    protected val r2 = DynoKey<Int>("r2")
    protected val r3 = DynoKey<List<Boolean>>("r3")

    protected object SimpleSchema: SimpleSchemaSerializer()

    protected open class SimpleSchemaSerializer: TestEagerSerializer<DynamicObject>() {
        val name = key<String>("name")
        val age = key<Int>("age")
        val tags = key<List<String>>("tags")
    }
}


class Point(val x : Int, val y : Int) {

    fun describeIf(predicate : () -> Boolean ) {
        if (predicate) {
            println("I am a point hoorey!!")
        }
    }

    operator fun plus(otherPoint : Point) = Point(this.x + otherPoint.x, this.y + otherPoint.y)


    override fun toString() = "(x = $x, y = $y)"
}

fun main() {
    val point1 = Point(2, 4)
    val point2 = Point(4, 6)
    point1.describeIf { 5 >= 5 }
       println("$point1 + $point2 = ${point1 + point2}")
}


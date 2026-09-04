package korlibs.math.geom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RectangleTest {
    @Test
    fun name() {
        val big = Rectangle.fromBounds(0, 0, 50, 50)
        val small = Rectangle.fromBounds(10, 10, 20, 20)
        val out = Rectangle.fromBounds(100, 10, 200, 20)
        assertTrue(small in big)
        assertTrue(big !in small)
        assertTrue(small == (small intersection big))
        assertTrue(small == (big intersection small))
        assertTrue(null == (big intersection out))
        assertTrue(small intersects big)
        assertTrue(big intersects small)
        assertFalse(big intersects out)
    }

    @Test
    fun name2() {
        val r1 = Rectangle(20, 0, 30, 10)
        val r2 = Rectangle(100, 0, 100, 50)
        val ro = r1.copy()
        ro.setToAnchoredRectangle(ro, Anchor.MIDDLE_CENTER, r2)
        assertEquals(Rectangle(135, 20, 30, 10), ro)
    }

    @Test
    fun testPlace() {
        val out = Rectangle(0, 0, 100, 100).place(Size(50, 25), Anchor.MIDDLE_CENTER, ScaleMode.SHOW_ALL)
        assertEquals(Rectangle(0, 25, 100, 50), out)
    }

    @Test
    fun corners() {
        val rectangle = Rectangle(1, 20, 300, 4000)
        assertEquals(Point(1, 20), rectangle.topLeft)
        assertEquals(Point(301, 20), rectangle.topRight)
        assertEquals(Point(1, 4020), rectangle.bottomLeft)
        assertEquals(Point(301, 4020), rectangle.bottomRight)

        val iRectangle = Rectangle(1000, 200, 30, 4)
        assertEquals(Point(1000, 200), iRectangle.topLeft)
        assertEquals(Point(1030, 200), iRectangle.topRight)
        assertEquals(Point(1000, 204), iRectangle.bottomLeft)
        assertEquals(Point(1030, 204), iRectangle.bottomRight)
    }

    @Test
    fun containsPointInside() {
        val rect = Rectangle(10, 20, 100, 200)
        val point = PointInt(11, 21)

        assertTrue(point.double in rect)
        assertTrue(point in rect)
        assertTrue(rect.contains(point.x.toDouble(), point.y.toDouble()))
        assertTrue(rect.contains(point.x.toFloat(), point.y.toFloat()))
        assertTrue(rect.contains(point.x, point.y))
    }

    @Test
    fun doesNotContainPointToTheLeft() {
        val rect = Rectangle(10, 20, 100, 200)
        val point = PointInt(9, 21)

        assertFalse(point.double in rect)
        assertFalse(point in rect)
        assertFalse(rect.contains(point.x.toDouble(), point.y.toDouble()))
        assertFalse(rect.contains(point.x.toFloat(), point.y.toFloat()))
        assertFalse(rect.contains(point.x, point.y))
    }

    @Test
    fun doesNotContainPointToTheTop() {
        val rect = Rectangle(10, 20, 100, 200)
        val point = PointInt(11, 19)

        assertFalse(point.double in rect)
        assertFalse(point in rect)
        assertFalse(rect.contains(point.x.toDouble(), point.y.toDouble()))
        assertFalse(rect.contains(point.x.toFloat(), point.y.toFloat()))
        assertFalse(rect.contains(point.x, point.y))
    }

    @Test
    fun doesNotContainPointToTheRight() {
        val rect = Rectangle(10, 20, 100, 200)
        val point = PointInt(110, 21)

        assertFalse(point.double in rect)
        assertFalse(point in rect)
        assertFalse(rect.contains(point.x.toDouble(), point.y.toDouble()))
        assertFalse(rect.contains(point.x.toFloat(), point.y.toFloat()))
        assertFalse(rect.contains(point.x, point.y))
    }

    @Test
    fun doesNotContainPointToTheBottom() {
        val rect = Rectangle(10, 20, 100, 200)
        val point = PointInt(11, 220)

        assertFalse(point.double in rect)
        assertFalse(point in rect)
        assertFalse(rect.contains(point.x.toDouble(), point.y.toDouble()))
        assertFalse(rect.contains(point.x.toFloat(), point.y.toFloat()))
        assertFalse(rect.contains(point.x, point.y))
    }

    @Test
    fun testMargin() {
        assertEquals(
            Rectangle.fromBounds(10, 10, 90, 90),
            Rectangle(0, 0, 100, 100).without(Margin(10f))
        )
        assertEquals(
            Rectangle.fromBounds(-10, -10, 110, 110),
            Rectangle(0, 0, 100, 100).with(Margin(10f))
        )
    }

    @Test
    fun testInt() {
        assertEquals(RectangleInt(1, 2, 3, 4), Rectangle(1.1, 2.1, 3.1, 4.1).toInt())
    }

    @Test
    fun testExpand() {
        assertEquals(
            Rectangle.fromBounds(-10, -15, 120, 125),
            Rectangle.fromBounds(0, 0, 100, 100).expand(10, 15, 20, 25)
        )
        assertEquals(
            Rectangle.fromBounds(-10, -15, 120, 125),
            Rectangle.fromBounds(0, 0, 100, 100)
                .expand(Margin(left = 10f, top = 15f, right = 20f, bottom = 25f))
        )
        assertEquals(
            Rectangle.fromBounds(-10, -15, 120, 125),
            Rectangle.fromBounds(0, 0, 100, 100)
                .expand(MarginInt(left = 10, top = 15, right = 20, bottom = 25))
        )
    }

    @Test
    fun constructWithPoints() {
        assertEquals(
            Rectangle(Point(0, 0), Point(100, 100)),
            Rectangle(0, 0, 100, 100)
        )
        assertEquals(
            Rectangle(Point(100, 100), Point(0, 0)),
            Rectangle(0, 0, 100, 100)
        )
        assertEquals(
            Rectangle(Point(0, 100), Point(100, 0)),
            Rectangle(0, 0, 100, 100)
        )
        assertEquals(
            Rectangle(Point(100, 0), Point(0, 100)),
            Rectangle(0, 0, 100, 100)
        )
    }

    @Test
    fun testRectangle() {
        val rect = Rectangle(1, 2, 3, 4)
        assertEquals(Point(1, 2), rect.position)
        assertEquals(Size(3, 4), rect.size)
        assertEquals(1.0, rect.x)
        assertEquals(2.0, rect.y)
        assertEquals(3.0, rect.width)
        assertEquals(4.0, rect.height)
    }
}

package org.opentrafficsim.i4driving.opendrive;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.djunits.value.vdouble.scalar.Angle;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.core.geometry.ContinuousArc;

/**
 * Overrides ContinuousArc to fix bug in endPoint() method.
 * @author wjschakel
 */
public class ContinuousArc2 extends ContinuousArc
{

    /**
     * Define arc by starting point, radius, curve direction, and length.
     * @param startPoint OrientedPoint2d; starting point.
     * @param radius double; radius (must be positive).
     * @param left boolean; left curve, or right.
     * @param length double; arc length.
     */
    public ContinuousArc2(OrientedPoint2d startPoint, double radius, boolean left, double length)
    {
        super(startPoint, radius, left, length);
    }

    @Override
    public OrientedPoint2d getEndPoint()
    {
        try
        {
            Method method = ContinuousArc.class.getDeclaredMethod("getPoint", double.class, double.class);
            method.setAccessible(true);
            Point2d point = (Point2d) method.invoke(this, 1.0, 0.0); // getPoint(1.0, 0.0);

            Field signField = ContinuousArc.class.getDeclaredField("sign");
            signField.setAccessible(true);
            double sign = signField.getDouble(this); // this.sign

            Field angleField = ContinuousArc.class.getDeclaredField("angle");
            angleField.setAccessible(true);
            Angle angle = (Angle) angleField.get(this); // this.angle

            double dirZ = getStartPoint().dirZ + sign * angle.si;
            dirZ = dirZ > Math.PI ? dirZ - 2.0 * Math.PI : (dirZ < -Math.PI ? dirZ + 2.0 * Math.PI : dirZ); // bug in parent
            return new OrientedPoint2d(point.x, point.y, dirZ);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException
                | SecurityException ex)
        {
            throw new RuntimeException("Unable to create endPoint for ContinuousArc");
        }
    }

}

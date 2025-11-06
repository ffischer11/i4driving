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
	private static Method getPoint_;
	private static Field sign_;
	private static Field angle_;
	static {
		try {
			getPoint_ = ContinuousArc.class.getDeclaredMethod("getPoint", double.class, double.class);
			getPoint_.setAccessible(true);
			
			sign_ = ContinuousArc.class.getDeclaredField("sign");
			sign_.setAccessible(true);

			angle_ = ContinuousArc.class.getDeclaredField("angle");
			angle_.setAccessible(true);
			
		} catch (NoSuchMethodException | NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("Unable to initialize ContinuousArc2");
		}
	}

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
            Point2d point = (Point2d) getPoint_.invoke(this, 1.0, 0.0); // getPoint(1.0, 0.0);

            double sign = sign_.getDouble(this); // this.sign

            Angle angle = (Angle) angle_.get(this); // this.angle

            double dirZ = getStartPoint().dirZ + sign * angle.si;
            dirZ = dirZ > Math.PI ? dirZ - 2.0 * Math.PI : (dirZ < -Math.PI ? dirZ + 2.0 * Math.PI : dirZ); // bug in parent
            return new OrientedPoint2d(point.x, point.y, dirZ);
        }
        catch (IllegalAccessException | InvocationTargetException | SecurityException ex)
        {
            throw new RuntimeException("Unable to create endPoint for ContinuousArc");
        }
    }

}

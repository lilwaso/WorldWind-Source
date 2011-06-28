/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.worldwind.view;

/**
 *
 * @author SLi
 */

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.ViewStateIterator;
import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.RestorableSupport;
import gov.nasa.worldwind.util.RestorableSupport.StateObject;
import java.util.logging.Logger;
import javax.media.opengl.GL;

public abstract class AbstractView extends WWObjectImpl
  implements View
{
  private boolean detectCollisions = true;
  protected boolean hadCollisions;
  protected ViewStateIterator viewStateIterator;

  public boolean isDetectCollisions()
  {
    return this.detectCollisions;
  }

  public void setDetectCollisions(boolean paramBoolean)
  {
    this.detectCollisions = paramBoolean;
  }

  public boolean hadCollisions()
  {
    boolean bool = this.hadCollisions;
    this.hadCollisions = false;
    return bool;
  }

  protected void flagHadCollisions()
  {
    this.hadCollisions = true;
  }

  public void stopMovement()
  {
    forceStopStateIterators();
    firePropertyChange("gov.nasa.worldwind.View.ViewStopped", null, this);
  }

  public void apply(DrawContext paramDrawContext)
  {
    String str;
    if (paramDrawContext == null)
    {
      str = Logging.getMessage("nullValue.DrawContextIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    if (paramDrawContext.getGL() == null)
    {
      str = Logging.getMessage("nullValue.DrawingContextGLIsNull");
      Logging.logger().severe(str);
      throw new IllegalStateException(str);
    }
    if (paramDrawContext.getGlobe() == null)
    {
      str = Logging.getMessage("layers.AbstractLayer.NoGlobeSpecifiedInDrawingContext");
      Logging.logger().severe(str);
      throw new IllegalStateException(str);
    }
    updateStateIterator();
    doApply(paramDrawContext);
  }

  protected abstract void doApply(DrawContext paramDrawContext);

  public Matrix pushReferenceCenter(DrawContext paramDrawContext, Vec4 paramVec4)
  {
    if (paramDrawContext == null)
    {
            String localObject = Logging.getMessage("nullValue.DrawContextIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramDrawContext.getGL() == null)
    {
            String localObject = Logging.getMessage("nullValue.DrawingContextGLIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalStateException((String)localObject);
    }
    if (paramVec4 == null)
    {
            String localObject = Logging.getMessage("nullValue.PointIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = getModelviewMatrix();
    Matrix localMatrix = null;
    if (localObject != null)
      localMatrix = ((Matrix)localObject).multiply(Matrix.fromTranslation(paramVec4));
    GL localGL = paramDrawContext.getGL();
    int[] arrayOfInt = new int[1];
    localGL.glGetIntegerv(2976, arrayOfInt, 0);
    if (arrayOfInt[0] != 5888)
      localGL.glMatrixMode(5888);
    localGL.glPushMatrix();
    if (localMatrix != null)
    {
      double[] arrayOfDouble = new double[16];
      localMatrix.toArray(arrayOfDouble, 0, false);
      localGL.glLoadMatrixd(arrayOfDouble, 0);
    }
    if (arrayOfInt[0] != 5888)
      localGL.glMatrixMode(arrayOfInt[0]);
    return (Matrix)localMatrix;
  }

  public void popReferenceCenter(DrawContext paramDrawContext)
  {
    if (paramDrawContext == null)
    {
            String localObject = Logging.getMessage("nullValue.DrawContextIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramDrawContext.getGL() == null)
    {
            String localObject = Logging.getMessage("nullValue.DrawingContextGLIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalStateException((String)localObject);
    }
    Object localObject = paramDrawContext.getGL();
    int[] arrayOfInt = new int[1];
    ((GL)localObject).glGetIntegerv(2976, arrayOfInt, 0);
    if (arrayOfInt[0] != 5888)
      ((GL)localObject).glMatrixMode(5888);
    ((GL)localObject).glPopMatrix();
    if (arrayOfInt[0] != 5888)
      ((GL)localObject).glMatrixMode(arrayOfInt[0]);
  }

  public void applyStateIterator(ViewStateIterator paramViewStateIterator)
  {
    ViewStateIterator localViewStateIterator = this.viewStateIterator;
    this.viewStateIterator = paramViewStateIterator;
    if (this.viewStateIterator != null)
    {
      this.viewStateIterator = this.viewStateIterator.coalesceWith(this, localViewStateIterator);
      firePropertyChange("gov.nasa.worldwind.avkey.ViewObject", null, this);
    }
  }

  public boolean hasStateIterator()
  {
    return this.viewStateIterator != null;
  }

  public void stopStateIterators()
  {
    forceStopStateIterators();
  }

  public String getRestorableState()
  {
    RestorableSupport localRestorableSupport = RestorableSupport.newRestorableSupport();
    if (localRestorableSupport == null)
      return null;
    doGetRestorableState(localRestorableSupport, null);
    return localRestorableSupport.getStateAsXml();
  }

  public void restoreState(String paramString)
  {
    Object localObject;
    if (paramString == null)
    {
      localObject = Logging.getMessage("nullValue.StringIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    try
    {
      localObject = RestorableSupport.parse(paramString);
    }
    catch (Exception localException)
    {
      String str = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", paramString);
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str, localException);
    }
    doRestoreState((RestorableSupport)localObject, null);
  }

  protected void updateStateIterator()
  {
    if (this.viewStateIterator != null)
      if (this.viewStateIterator.hasNextState(this))
      {
        this.viewStateIterator.nextState(this);
        firePropertyChange("gov.nasa.worldwind.avkey.ViewObject", null, this);
      }
      else
      {
        forceStopStateIterators();
        firePropertyChange("gov.nasa.worldwind.avkey.ViewQuiet", null, this);
      }
  }

  protected void forceStopStateIterators()
  {
    this.viewStateIterator = null;
  }

  protected void doGetRestorableState(RestorableSupport paramRestorableSupport, RestorableSupport.StateObject paramStateObject)
  {
    paramRestorableSupport.addStateValueAsBoolean(paramStateObject, "detectCollisions", isDetectCollisions());
  }

  protected void doRestoreState(RestorableSupport paramRestorableSupport, RestorableSupport.StateObject paramStateObject)
  {
    Boolean localBoolean = paramRestorableSupport.getStateValueAsBoolean(paramStateObject, "detectCollisions");
    if (localBoolean != null)
      setDetectCollisions(localBoolean.booleanValue());
  }
}

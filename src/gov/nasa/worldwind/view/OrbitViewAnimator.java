/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.worldwind.view;

public abstract interface OrbitViewAnimator
{
  public abstract void doNextState(double paramDouble, OrbitView paramOrbitView, BasicOrbitViewStateIterator paramBasicOrbitViewStateIterator);

  public abstract OrbitViewAnimator coalesceWith(OrbitView paramOrbitView, OrbitViewAnimator paramOrbitViewAnimator);

  public abstract void setStopOnInvalidState(boolean paramBoolean);

  public abstract boolean isStopOnInvalidState();
}
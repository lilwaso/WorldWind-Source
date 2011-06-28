/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.worldwind.view;

public abstract interface OrbitViewInterpolator
{
  public abstract double nextInterpolant(OrbitView paramOrbitView);

  public abstract OrbitViewInterpolator coalesceWith(OrbitView paramOrbitView, OrbitViewInterpolator paramOrbitViewInterpolator);
}
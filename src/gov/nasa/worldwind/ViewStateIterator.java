/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.worldwind;

public abstract interface ViewStateIterator
{
  public abstract ViewStateIterator coalesceWith(View paramView, ViewStateIterator paramViewStateIterator);

  public abstract boolean hasNextState(View paramView);

  public abstract void nextState(View paramView);
}
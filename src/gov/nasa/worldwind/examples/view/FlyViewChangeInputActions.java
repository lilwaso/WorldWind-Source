/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples.view;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.awt.ViewInputAttributes;
import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.view.firstperson.*;

import java.awt.event.*;

/**
 * Example that demonstrates how to change the user input controls for a {@link BasicFlyView}. This example maps mouse
 * button 3 to control horizontal translation, and mouse button 1 to control view rotation (heading and pitch).
 * 
 * @author jym
 * @version $Id: FlyViewChangeInputActions.java 14514 2011-01-19 18:19:27Z pabercrombie $
 */
public class FlyViewChangeInputActions extends ApplicationTemplate
{
    /**
     * Custom view input handler.
     */
    protected static class MyFlyViewInputHandler extends FlyViewInputHandler
    {
        /**
         * Create the custom input handler.
         */
        public MyFlyViewInputHandler()
        {
            // This constructor customizes the FlyViewInputHandler input mappings. The input handler maintains
            // a table that maps input events (keyboard and mouse events) to view actions (translate, rotate, etc).
            
            // Create an array of the input actions that will change the horizontal translation
            ViewInputAttributes.ActionAttributes.MouseAction[] mouseActionTrans = {
                ViewInputAttributes.ActionAttributes.createMouseActionAttribute(MouseEvent.BUTTON3_DOWN_MASK)
            };

            // Map the input actions to the desired behavior. This code maps the input event "mouse button 3 down" to
            // the action VIEW_HORIZONTAL_TRANSLATE.
            this.getAttributes().setMouseActionAttributes(
                ViewInputAttributes.VIEW_HORIZONTAL_TRANSLATE, // Action to map to mouse button 3
                0, // Modifiers, none in this case
                ViewInputAttributes.ActionAttributes.ActionTrigger.ON_KEY_DOWN, // The event that triggers the action
                mouseActionTrans, // Input actions to map to the behavior, in this case mouse button 3
                FlyViewInputHandler.DEFAULT_MOUSE_HORIZONTAL_TRANSLATE_MIN_VALUE,
                FlyViewInputHandler.DEFAULT_MOUSE_HORIZONTAL_TRANSLATE_MAX_VALUE,
                true, // Enable smoothing
                0.7); // Smoothing value

            // Create an array of input events to that will change rotation.
            ViewInputAttributes.ActionAttributes.MouseAction[] mouseActionRot = {
                ViewInputAttributes.ActionAttributes.createMouseActionAttribute(MouseEvent.BUTTON1_DOWN_MASK)
            };

            // Map the input events to the action VIEW_ROTATE
            this.getAttributes().setMouseActionAttributes(ViewInputAttributes.VIEW_ROTATE, 0,
                ViewInputAttributes.ActionAttributes.ActionTrigger.ON_KEY_DOWN,
                mouseActionRot,
                FlyViewInputHandler.DEFAULT_MOUSE_ROTATE_MIN_VALUE,
                FlyViewInputHandler.DEFAULT_MOUSE_ROTATE_MAX_VALUE,
                true, .7);
        }
    }

    public static void main(String[] args)
    {
        // Call the static start method like this from the main method of your derived class.
        // Substitute your application's name for the first argument.
        ApplicationTemplate.AppFrame appFrame = ApplicationTemplate.start("World Wind Fly View Controls",
            ApplicationTemplate.AppFrame.class);

        WorldWindow wwd = appFrame.getWwd();

        // Force the view to be a FlyView
        BasicFlyView flyView = new BasicFlyView();
        flyView.setViewInputHandler(new MyFlyViewInputHandler());
        wwd.setView(flyView);
    }
}

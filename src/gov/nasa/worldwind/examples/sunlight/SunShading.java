package gov.nasa.worldwind.examples.sunlight;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.examples.LayerPanel;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Earth.USGSTopographicMaps;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SunShading extends ApplicationTemplate
{
  public static void main(String[] paramArrayOfString)
  {
    Configuration.setValue("gov.nasa.worldwind.avkey.TessellatorClassName", RectangularNormalTessellator.class.getName());
    ApplicationTemplate.start("World Wind Sun Shading", AppFrame.class);
  }

  public static class AppFrame extends ApplicationTemplate.AppFrame
  {
    private JCheckBox enableCheckBox;
    private JButton colorButton;
    private JButton ambientButton;
    private JRadioButton relativeRadioButton;
    private JRadioButton absoluteRadioButton;
    private JSlider azimuthSlider;
    private JSlider elevationSlider;
    private RectangularNormalTessellator tessellator;
    private LensFlareLayer lensFlareLayer;
    private AtmosphereLayer atmosphereLayer;
    private SunPositionProvider spp = new BasicSunPositionProvider();

    public AppFrame()
    {
      super(true, false, false);
      ApplicationTemplate.insertBeforePlacenames(getWwd(), new USGSTopographicMaps());
      this.atmosphereLayer = new AtmosphereLayer();
      for (int i = 0; i < getWwd().getModel().getLayers().size(); i++)
      {
        Layer localLayer = (Layer)getWwd().getModel().getLayers().get(i);
        if (!(localLayer instanceof SkyGradientLayer))
          continue;
        getWwd().getModel().getLayers().set(i, this.atmosphereLayer);
      }
      this.lensFlareLayer = LensFlareLayer.getPresetInstance("LensFlare.PresetBold");
      getWwd().getModel().getLayers().add(this.lensFlareLayer);
      getLayerPanel().update(getWwd());
      this.tessellator = ((RectangularNormalTessellator)getWwd().getModel().getGlobe().getTessellator());
      getLayerPanel().add(makeControlPanel(), "South");
      getWwd().addPositionListener(new PositionListener()
      {
        Vec4 eyePoint;

        public void moved(PositionEvent paramPositionEvent)
        {
          if ((this.eyePoint == null) || (this.eyePoint.distanceTo3(SunShading.AppFrame.this.getWwd().getView().getEyePoint()) > 1000.0D))
          {
            SunShading.AppFrame.this.update();
            this.eyePoint = SunShading.AppFrame.this.getWwd().getView().getEyePoint();
          }
        }
      });
      Timer localTimer = new Timer(60000, new ActionListener()
      {
        public void actionPerformed(ActionEvent paramActionEvent)
        {
          SunShading.AppFrame.this.update();
        }
      });
      localTimer.start();
      update();
    }

    private JPanel makeControlPanel()
    {
      JPanel localJPanel1 = new JPanel();
      localJPanel1.setLayout(new BoxLayout(localJPanel1, 1));
      localJPanel1.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Sun Light")));
      localJPanel1.setToolTipText("Set the Sun light direction and color");
      JPanel localJPanel2 = new JPanel(new GridLayout(0, 3, 0, 0));
      localJPanel2.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
      this.enableCheckBox = new JCheckBox("Enable");
      this.enableCheckBox.setSelected(true);
      this.enableCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent paramActionEvent)
        {
          SunShading.AppFrame.this.update();
        }
      });
      localJPanel2.add(this.enableCheckBox);
      this.colorButton = new JButton("Light");
      this.colorButton.setBackground(this.tessellator.getLightColor());
      this.colorButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent paramActionEvent)
        {
          JPanel panel = new JPanel();
          Color localColor = JColorChooser.showDialog(panel, "Choose a color...", ((JButton)paramActionEvent.getSource()).getBackground());
          
          if (localColor != null)
          {
            ((JButton)paramActionEvent.getSource()).setBackground(localColor);
            SunShading.AppFrame.this.update();
          }
        }
      });
      localJPanel2.add(this.colorButton);
      this.ambientButton = new JButton("Shade");
      this.ambientButton.setBackground(this.tessellator.getAmbientColor());
      this.ambientButton.addActionListener(new ActionListener()
              
      {
        public void actionPerformed(ActionEvent paramActionEvent)
        {
          JPanel panel = new JPanel();
          Color localColor = JColorChooser.showDialog(panel, "Choose a color...", ((JButton)paramActionEvent.getSource()).getBackground());
          if (localColor != null)
          {
            ((JButton)paramActionEvent.getSource()).setBackground(localColor);
            SunShading.AppFrame.this.update();
          }
        }
      });
      localJPanel2.add(this.ambientButton);
      JPanel localJPanel3 = new JPanel(new GridLayout(0, 2, 0, 0));
      localJPanel3.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
      this.relativeRadioButton = new JRadioButton("Relative");
      this.relativeRadioButton.setSelected(false);
      this.relativeRadioButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent paramActionEvent)
        {
          SunShading.AppFrame.this.update();
        }
      });
      localJPanel3.add(this.relativeRadioButton);
      this.absoluteRadioButton = new JRadioButton("Absolute");
      this.absoluteRadioButton.setSelected(true);
      this.absoluteRadioButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent paramActionEvent)
        {
          SunShading.AppFrame.this.update();
        }
      });
      localJPanel3.add(this.absoluteRadioButton);
      ButtonGroup localButtonGroup = new ButtonGroup();
      localButtonGroup.add(this.relativeRadioButton);
      localButtonGroup.add(this.absoluteRadioButton);
      JPanel localJPanel4 = new JPanel(new GridLayout(0, 1, 0, 0));
      localJPanel4.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
      localJPanel4.add(new JLabel("Azimuth:"));
      this.azimuthSlider = new JSlider(0, 360, 125);
      this.azimuthSlider.setPaintTicks(true);
      this.azimuthSlider.setPaintLabels(true);
      this.azimuthSlider.setMajorTickSpacing(90);
      this.azimuthSlider.addChangeListener(new ChangeListener()
      {
        public void stateChanged(ChangeEvent paramChangeEvent)
        {
          SunShading.AppFrame.this.update();
        }
      });
      localJPanel4.add(this.azimuthSlider);
      JPanel localJPanel5 = new JPanel(new GridLayout(0, 1, 0, 0));
      localJPanel5.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
      localJPanel5.add(new JLabel("Elevation:"));
      this.elevationSlider = new JSlider(-10, 90, 50);
      this.elevationSlider.setPaintTicks(true);
      this.elevationSlider.setPaintLabels(true);
      this.elevationSlider.setMajorTickSpacing(10);
      this.elevationSlider.addChangeListener(new ChangeListener()
      {
        public void stateChanged(ChangeEvent paramChangeEvent)
        {
          SunShading.AppFrame.this.update();
        }
      });
      localJPanel5.add(this.elevationSlider);
      localJPanel1.add(localJPanel2);
      localJPanel1.add(localJPanel3);
      localJPanel1.add(localJPanel4);
      localJPanel1.add(localJPanel5);
      return localJPanel1;
    }

    private void update()
    {
      if (this.enableCheckBox.isSelected())
      {
        this.colorButton.setEnabled(true);
        this.ambientButton.setEnabled(true);
        this.absoluteRadioButton.setEnabled(true);
        this.relativeRadioButton.setEnabled(true);
        this.azimuthSlider.setEnabled(true);
        this.elevationSlider.setEnabled(true);
        this.tessellator.setLightColor(this.colorButton.getBackground());
        this.tessellator.setAmbientColor(this.ambientButton.getBackground());
        Object localObject;
        Vec4 localVec41;
        if (this.relativeRadioButton.isSelected())
        {
          this.azimuthSlider.setEnabled(true);
          this.elevationSlider.setEnabled(true);
          localObject = Angle.fromDegrees(this.elevationSlider.getValue());
          Angle localAngle = Angle.fromDegrees(this.azimuthSlider.getValue());
          Position localPosition = getWwd().getView().getEyePosition();
          localVec41 = Vec4.UNIT_Y;
          localVec41 = localVec41.transformBy3(Matrix.fromRotationX((Angle)localObject));
          localVec41 = localVec41.transformBy3(Matrix.fromRotationZ(localAngle.multiply(-1.0D)));
          localVec41 = localVec41.transformBy3(getWwd().getModel().getGlobe().computeTransformToPosition(localPosition.getLatitude(), localPosition.getLongitude(), 0.0D));
        }
        else
        {
          this.azimuthSlider.setEnabled(false);
          this.elevationSlider.setEnabled(false);
          localObject = this.spp.getPosition();
          localVec41 = getWwd().getModel().getGlobe().computePointFromPosition(new Position((LatLon)localObject, 0.0D)).normalize3();
        }
        Vec4 localVec42 = localVec41.getNegative3();
        this.tessellator.setLightDirection(localVec42);
        this.lensFlareLayer.setSunDirection(localVec41);
        this.atmosphereLayer.setSunDirection(localVec41);
      }
      else
      {
        this.colorButton.setEnabled(false);
        this.ambientButton.setEnabled(false);
        this.absoluteRadioButton.setEnabled(false);
        this.relativeRadioButton.setEnabled(false);
        this.azimuthSlider.setEnabled(false);
        this.elevationSlider.setEnabled(false);
        this.tessellator.setLightDirection(null);
        this.lensFlareLayer.setSunDirection(null);
        this.atmosphereLayer.setSunDirection(null);
      }
      getWwd().redraw();
    }
  }
}
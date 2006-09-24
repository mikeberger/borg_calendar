package net.sf.borg.ui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.*;

import javax.swing.JPanel;

import net.sf.borg.common.util.Errmsg;

public class ApptBoxPanel extends JPanel
{

    final static private BasicStroke highlight = new BasicStroke(2.0f);

    final static private BasicStroke regular = new BasicStroke(1.0f);
    
    final static private int translation = 10;
    
    private int resizeMin = 0;
    private int resizeMax = 0;
    public void setResizeBounds(int min,int max)
    {
        resizeMin = min;
        resizeMax = max;
    }

    private class MyMouseListener implements MouseListener, MouseMotionListener
    {

        private Box draggedBox = null;
        private boolean resizeTop = true;
        
        public MyMouseListener()
        {

        }

        public void mouseClicked(MouseEvent evt)
        {

            //if (evt.getClickCount() < 2)
               // return;

            ClickedBoxInfo b = getBox(evt);
            if (b == null)
            {
                // nothing
            }
            else if (b.box.type == Box.DATEZONE)
            {
                b.box.model.dblClick();
            }
            else
            {              
                b.box.model.dblClick();
            }

            //evt.getComponent().getParent().repaint();
        }

        public void mousePressed(MouseEvent arg0)
        {
            ClickedBoxInfo b = getBox(arg0);
            if( b != null && !b.box.model.isOutsideGrid() && (b.onTopBorder || b.onBottomBorder))
            {
                draggedBox = b.box;  
                setResizeBox(b.box.x,b.box.y,b.box.w,b.box.h);
                if( b.onBottomBorder)
                {
                    resizeTop = false;
                }
                else
                {
                    resizeTop = true;
                }
            }
        }

        public void mouseReleased(MouseEvent arg0)
        {
            if( draggedBox != null )
            {
                int y = arg0.getY();
                y = Math.max(y,resizeMin);
                y = Math.min(y,resizeMax);
                try
                {
                    draggedBox.model.resize(resizeTop,(double)(y-resizeMin)/(double)(resizeMax-resizeMin));
                }
                catch (Exception e)
                {
                   Errmsg.errmsg(e);
                }
                draggedBox = null;
                removeResizeBox();
                arg0.getComponent().getParent().repaint();
            }
        
        }

        public void mouseEntered(MouseEvent arg0)
        {
        }

        public void mouseExited(MouseEvent arg0)
        {
        }

        public void mouseDragged(MouseEvent arg0)
        {
            
            if( draggedBox != null )
            {               
                if( resizeTop == true)
                {
                    int top = Math.max(arg0.getY(), resizeMin);
                    setResizeBox(draggedBox.x,top,draggedBox.w,draggedBox.h+draggedBox.y-top);
                }
                else
                {
                    int bot = Math.min(arg0.getY(),resizeMax);
                    setResizeBox(draggedBox.x,draggedBox.y,draggedBox.w,bot-draggedBox.y);
                }
                arg0.getComponent().repaint();
            }          
        }
        

        private class ClickedBoxInfo
        {
            public Box box;

            public boolean onTopBorder;
            public boolean onBottomBorder;
        }

        private ClickedBoxInfo getBox(MouseEvent evt)
        {
            //			 determine which box is selected, if any
            Iterator it = boxes.iterator();
            Box boxFound = null;
            Box zone = null;
            boolean onTopBorder = false;
            boolean onBottomBorder = false;
            while (it.hasNext())
            {

                Box b = (Box) it.next();

                // this is bad magic
                int realx = (int) (b.x + translation);
                int realy = (int) (b.y + translation);
                int realh = (int) (b.h );
                int realw = (int) (b.w);

                // System.out.println(b.layout.getAppt().getText() + " " + realx
                // + " " + realy + " " + realw + " " + realh);
                if (evt.getX() > realx && evt.getX() < (realx + realw)
                        && evt.getY() > realy && evt.getY() < (realy + realh))
                {

                    if (b.type == Box.DATEZONE)
                    {
                        zone = b;
                        continue;
                    }

                    b.model.setSelected(true);
                    boxFound = b;
                    if (Math.abs(evt.getY() - realy) < 3 )
                    {
                        onTopBorder = true;
                    }
                    else if( Math.abs(evt.getY() - (realy + realh)) < 3)
                    {
                        onBottomBorder = true;
                    }
                }
                else if (b.type == Box.APPTBOX)
                {
                    b.model.setSelected(false);
                }
            }

            ClickedBoxInfo ret = new ClickedBoxInfo();
            if (boxFound != null)
                ret.box = boxFound;
            else if (zone != null)
                ret.box = zone;
            else
                return null;

            ret.onTopBorder = onTopBorder;
            ret.onBottomBorder = onBottomBorder;

            return ret;

        }

        public void mouseMoved(MouseEvent evt)
        {

           
            JPanel panel = (JPanel) evt.getComponent();

            ClickedBoxInfo b = getBox(evt);
            if (b != null && b.box.type == Box.APPTBOX)
            {
                panel.setToolTipText(b.box.model.getText());
            }

            
             if ( b != null && (b.onTopBorder || b.onBottomBorder) ) {
                 panel.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
             } else if(b != null && b.box.type == Box.APPTBOX ){
                 panel.setCursor(new Cursor(Cursor.TEXT_CURSOR));
             } else {
                 panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
             }

            evt.getComponent().getParent().repaint();
             

        }
    }

    public interface BoxModel
    {
        public void resize(boolean isTop, double y_fraction) throws Exception;

        public void setText(String s);

        public String getText();
        public String getTextColor();

        public void dblClick();
        
        public double getBottomAdjustment();
        public double getRightAdjustment();
        public double getTopAdjustment();
        public double getLeftAdjustment();
        public boolean isOutsideGrid();
        
        public boolean isSelected();
        public void setSelected(boolean b);
    }

    
    static private class Box
    {
        public static final int APPTBOX = 1;
        public static final int DATEZONE = 2;
        
        public int w;

        public int h;

        public int x;

        public int y;

        public BoxModel model;

        public Color color;

        public int type = APPTBOX;

    }

    private Collection boxes = new ArrayList();

    public ApptBoxPanel()
    {
        MyMouseListener myOneListener = new MyMouseListener();
        addMouseListener(myOneListener);
        addMouseMotionListener(myOneListener);
    };

    private static Rectangle resizeBox = null;
    
    public void setResizeBox(double x,double y,double w, double h)
    {
        if( resizeBox == null )
            resizeBox = new Rectangle();
        resizeBox.x = (int) x;
        resizeBox.y = (int) y;
        resizeBox.height = (int) h;
        resizeBox.width = (int) w;
        
        
    }
    public void removeResizeBox()
    {
        resizeBox = null;
    }
    
    public void addBox(BoxModel bm, double x, double y,
            double w, double h, Color c)
    {
        Box b = new Box();
        b.type = Box.APPTBOX;

        if (bm.isOutsideGrid())
        {
            b.x = (int) x;
            b.y = (int) y;
            b.h = (int) h;
            b.w = (int) w;
        }
        else
        {
            b.x = (int) (x + 2 + w * bm.getLeftAdjustment());
            b.y = (int) (y + h * bm.getTopAdjustment());
            b.h = (int) ((bm.getBottomAdjustment() - bm.getTopAdjustment()) * h);
            b.w = (int) ((bm.getRightAdjustment() - bm.getLeftAdjustment()) * w);
        }
        b.model = bm;
        b.color = c;

        boxes.add(b);
    }

    public void addDateZone(BoxModel bm, double x, double y, double w, double h)
    {
        Box b = new Box();
        b.type = Box.DATEZONE;
        b.x = (int) x;
        b.y = (int) y;
        b.h = (int) h;
        b.w = (int) w;
        b.model = bm;

        boxes.add(b);
    }

    public void drawBoxes(Graphics2D g2)
    {
        Shape s = g2.getClip();
        Font sm_font = g2.getFont();
        int smfontHeight = g2.getFontMetrics().getHeight();
        Map stmap = new HashMap();
        stmap.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        stmap.put(TextAttribute.FONT, sm_font);

        Stroke stroke = g2.getStroke();
        Iterator it = boxes.iterator();
        while (it.hasNext())
        {
            Box b = (Box) it.next();
            if (b.type == Box.DATEZONE)
                continue;           


            // add a single appt text
            // if the appt falls outside the grid - leave it as
            // a note on top
            if (b.model.isOutsideGrid())
            {

                // appt is note or is outside timespan shown
                g2.clipRect(b.x, 0, b.w, 1000);

                if(  b.model.getTextColor().equals("strike"))
                {
                   
                    AttributedString as = new AttributedString(b.model.getText(),
                            stmap);
                    g2.drawString(as.getIterator(), b.x + 2, b.y + smfontHeight);
                }
                else
                {
                    // g2.setFont(sm_font);
                    g2.drawString(b.model.getText(), b.x + 2, b.y + smfontHeight);
                }

                if (b.model.isSelected())
                {
                    g2.setStroke(regular);
                    g2.setColor(Color.BLUE);
                    g2.drawRect(b.x, b.y + 2, b.w, b.h);
                    g2.setStroke(stroke);
                }
                g2.setColor(Color.BLACK);
            }
            else
            {

                // fill the box with color
                g2.setColor(b.color);
                g2.fill3DRect(b.x, b.y, b.w, b.h, true);

                // draw box outline

                if (b.model.isSelected())
                {
                    g2.setStroke(highlight);
                    g2.setColor(Color.BLUE);
                    g2.drawRect(b.x, b.y, b.w, b.h);
                    g2.setStroke(stroke);

                }

                // draw the appt text
                g2.setColor(Color.BLACK);
                g2.clipRect(b.x, b.y, b.w, b.h);

                // add a single appt text

                // change color for a single appointment based on
                // its color - only if color print option set
                g2.setColor(Color.black);
                if (b.model.getTextColor().equals("red"))
                    g2.setColor(Color.red);
                else if (b.model.getTextColor().equals("green"))
                    g2.setColor(Color.green);
                else if (b.model.getTextColor().equals("blue"))
                    g2.setColor(Color.blue);

                drawWrappedString(g2, b.model.getText(), b.x, b.y, b.w);

            }
            g2.setClip(s);
            g2.setColor(Color.black);
        }

        if( resizeBox != null )
        {
            g2.setStroke(highlight);
            g2.setColor(Color.RED);
            g2.drawRect(resizeBox.x, resizeBox.y, resizeBox.width, resizeBox.height);
            g2.setStroke(stroke);
        }
        g2.setColor(Color.black);

    }

    public void clearBoxes()
    {
        boxes.clear();
    }

    private void drawWrappedString(Graphics2D g2, String tx, int x, int y, int w)
    {
        int fontDesent = g2.getFontMetrics().getDescent();
        HashMap hm = new HashMap();
        hm.put(TextAttribute.FONT, g2.getFont());
        AttributedString as = new AttributedString(tx, hm);
        AttributedCharacterIterator para = as.getIterator();
        int start = para.getBeginIndex();
        int endi = para.getEndIndex();
        LineBreakMeasurer lbm = new LineBreakMeasurer(para,
                new FontRenderContext(null, false, false));
        lbm.setPosition(start);
        int tt = y + 2;
        while (lbm.getPosition() < endi)
        {
            TextLayout tlayout = lbm.nextLayout(w - (2 * fontDesent));
            tt += tlayout.getAscent();
            tlayout.draw(g2, x + 2, tt);
            tt += tlayout.getDescent() + tlayout.getLeading();
        }
    }
   

}

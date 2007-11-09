package net.sf.borg.ui.calendar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;

public abstract class ApptBoxPanel extends JPanel
{

    final static private float hlthickness = 3.0f;
    final static private BasicStroke highlight = new BasicStroke(hlthickness);
    //final static private BasicStroke regular = new BasicStroke(1.0f);
    final static private BasicStroke thicker = new BasicStroke(4.0f);
    final static private int radius = 5;

    final static private int translation = -10; // to adjust, since since Graphics2D is translated 

    private JPopupMenu editmenu = null;
    private JPopupMenu addmenu = null;
    private UIBoxInfo popupBox;
    private UIBoxInfo draggedZone = null;
    private UIBoxInfo draggedBox = null;
    private boolean dragStarted = false;
    private int draggedAnchor = -1;
    

    private double resizeMin = 0;
    private double resizeMax = 0;
    
    abstract Date getDateForX(double x);

    public void setResizeBounds(int min, int max)
    {
        resizeMin = min;
        resizeMax = max;
    }

    private class MyMouseListener implements MouseListener, MouseMotionListener
    {

        
        private boolean resizeTop = true;
        private boolean isMove = false;

        public MyMouseListener()
        {

        }

        public void mouseClicked(MouseEvent evt)
        {
            
            evt.translatePoint(translation,translation);

            if (evt.getButton() == MouseEvent.BUTTON3)
            {
                ClickedBoxInfo b = getClickedBoxInfo(evt);
                popupBox = b.box;
                
                if( b.inDragNewBox || popupBox == null || popupBox.type == UIBoxInfo.DATEZONE )
                {
                    addmenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
                else
                {
                    editmenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
                return;
            }
            
            evt.getComponent().getParent().repaint(); // to remove drag box if needed

            if (evt.getClickCount() < 2)
                return;

            ClickedBoxInfo b = getClickedBoxInfo(evt);
            if (b == null)
            {
                // nothing
            }
            else if (b.box.type == UIBoxInfo.DATEZONE)
            {
                b.box.model.edit();
            }
            else
            {
                b.box.model.edit();
            }

            
        }

        public void mousePressed(MouseEvent evt)
        {
            
            evt.translatePoint(translation,translation);
            
            if (evt.getButton() == MouseEvent.BUTTON3)
                return;

            removeDragNewBox();
            dragStarted = false;
            ClickedBoxInfo b = getClickedBoxInfo(evt);
            if (b != null && !b.box.model.isOutsideGrid() && (b.onTopBorder || b.onBottomBorder))
            {
                draggedBox = b.box;
                setResizeBox(b.box.x, b.box.y, b.box.w, b.box.h);
                if (b.onBottomBorder)
                {
                    resizeTop = false;
                }
                else
                {
                    resizeTop = true;
                }
                isMove = false;
                evt.getComponent().getParent().repaint();
            }
            else if( b != null && !b.box.model.isOutsideGrid() && b.box.type == UIBoxInfo.APPTBOX)
            {
               isMove = true;
               draggedBox = b.box;
               setResizeBox(b.box.x, b.box.y, b.box.w, b.box.h);
               evt.getComponent().getParent().repaint();
            }
            else if (b != null && evt.getY() > resizeMin && evt.getY() < resizeMax)
            {
                // b is a date zone
                draggedZone = b.zone;
                //setDragNewBox(b.box.x, arg0.getY(), b.box.w, 2);
                draggedAnchor = evt.getY();
            }
        }

        public void mouseReleased(MouseEvent evt)
        {
            evt.translatePoint(translation,translation);
            
            if (evt.getButton() == MouseEvent.BUTTON3)
                return;
            if (draggedBox != null && !isMove && dragStarted)
            {
                double y = evt.getY();
                y = Math.max(y, resizeMin);
                y = Math.min(y, resizeMax);
                try
                {
                    draggedBox.model.resize(resizeTop, (y - resizeMin) / (resizeMax - resizeMin));
                }
                catch (Exception e)
                {
                    Errmsg.errmsg(e);
                }
                
            }
            else if( draggedBox != null && isMove && dragStarted)
            {
                double y = resizeBox.y;
                y = Math.max(y, resizeMin);
                y = Math.min(y, resizeMax);
                
                double centerx = evt.getX() + draggedBox.w/2;
                Date d = getDateForX(centerx);
                try
                {
                    draggedBox.model.move((y - resizeMin) / (resizeMax - resizeMin),d);
                }
                catch (Exception e)
                {
                    Errmsg.errmsg(e);
                }
             
            }
            
            draggedBox = null;
            removeResizeBox();
            evt.getComponent().getParent().repaint();           

        }

        public void mouseEntered(MouseEvent evt)
        {
        }

        public void mouseExited(MouseEvent evt)
        {
        }

        public void mouseDragged(MouseEvent evt)
        {
            evt.translatePoint(translation,translation);
            
            if (evt.getButton() == MouseEvent.BUTTON3)
                return;

            dragStarted = true;
            if (draggedBox != null)
            {
                if( isMove == true )
                {
                    int top = evt.getY()-(draggedBox.h/2);
                    if( top < resizeMin ) top = (int)resizeMin;
                    if( top + draggedBox.h > resizeMax) top = (int)resizeMax - draggedBox.h;
                    
                    setResizeBox(evt.getX(), top, draggedBox.w, draggedBox.h);
                    //setResizeBox(draggedBox.x, top, draggedBox.w, draggedBox.h);
                }
                else if (resizeTop == true)
                {
                    int top = (int) Math.max(evt.getY(), resizeMin);
                    setResizeBox(draggedBox.x, top, draggedBox.w, draggedBox.h + draggedBox.y - top);
                }
                else
                {
                    int bot = (int) Math.min(evt.getY(), resizeMax);
                    setResizeBox(draggedBox.x, draggedBox.y, draggedBox.w, bot - draggedBox.y);
                }
                evt.getComponent().repaint();
            }
            else if (draggedAnchor != -1)
            {
                if( dragNewBox == null)
                    setDragNewBox(draggedZone.x, evt.getY(), draggedZone.w, 5);
                double y = evt.getY();
                y = Math.max(y, resizeMin);
                y = Math.min(y, resizeMax);
                if (y > draggedAnchor)
                {
                    setDragNewBox(dragNewBox.x, dragNewBox.y, dragNewBox.width, y - draggedAnchor);
                }
                else
                {
                    setDragNewBox(dragNewBox.x, y, dragNewBox.width, draggedAnchor - y);
                }
                evt.getComponent().repaint();
            }
        }

        public void mouseMoved(MouseEvent evt)
        {
            evt.translatePoint(translation,translation);
            
            if (evt.getButton() == MouseEvent.BUTTON3)
                return;

            JPanel panel = (JPanel) evt.getComponent();

            ClickedBoxInfo b = getClickedBoxInfo(evt);
            if (b != null && b.box.type == UIBoxInfo.APPTBOX && !b.inDragNewBox)
            {
                panel.setToolTipText(b.box.model.getText());
            }

            if (b != null && (b.onTopBorder || b.onBottomBorder) && !b.inDragNewBox && !b.box.model.isOutsideGrid())
            {
                panel.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
            }
            else if (b != null && b.box.type == UIBoxInfo.APPTBOX && !b.inDragNewBox && !b.box.model.isOutsideGrid())
            {
                panel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
            }
            else
            {
                panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            evt.getComponent().getParent().repaint();

        }
    }

    public interface BoxModel
    {
        public void resize(boolean isTop, double y_fraction) throws Exception;
        
        public void move(double y_fraction, Date d) throws Exception;

        public void create(double top, double bottom, String text);

        public void delete();

        public void setText(String s);

        public String getText();

        public String getTextColor();

        public void edit();

        public double getBottomAdjustment();

        public double getRightAdjustment();

        public double getTopAdjustment();

        public double getLeftAdjustment();

        public boolean isOutsideGrid();

        public boolean isSelected();

        public void setSelected(boolean b);
        
        public String getTimeString(double y_fraction);
        
        public boolean isTodo();
    }

    static private class UIBoxInfo
    {
        public static final int APPTBOX = 1;
        public static final int DATEZONE = 2;

        public int w;

        public int h;

        public int x;

        public int y;

        public BoxModel model;

        public int boxnum;

        public int type = APPTBOX;

    }

    private class ClickedBoxInfo
    {
        public UIBoxInfo box;
        public UIBoxInfo zone;
        
        public boolean inDragNewBox;

        public boolean onTopBorder;
        public boolean onBottomBorder;
    }

    private Collection boxes = new ArrayList();

    public ApptBoxPanel()
    {
        final JPanel t = this;
        MyMouseListener myOneListener = new MyMouseListener();
        addMouseListener(myOneListener);
        addMouseMotionListener(myOneListener);

        JMenuItem mnuitm = null;
        addmenu = new JPopupMenu();
        addmenu.add(mnuitm = new JMenuItem(Resource.getPlainResourceString("Add_New")));
        mnuitm.addActionListener(new ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                if (dragNewBox != null)
                {
                    String text = JOptionPane.showInputDialog(t,Resource.getPlainResourceString("Please_enter_some_appointment_text"));
                    if( text == null ) return;
                    double top = (dragNewBox.y - resizeMin) / (resizeMax - resizeMin);
                    double bot = (dragNewBox.y - resizeMin + dragNewBox.height) / (resizeMax - resizeMin);
                    draggedZone.model.create(top, bot,text);
                    draggedZone = null;
                    removeDragNewBox();
                    repaint();
                }
                else
                {
                    popupBox.model.edit();
                }
            }
        });
        editmenu = new JPopupMenu();
        editmenu.add(mnuitm = new JMenuItem(Resource.getPlainResourceString("Edit")));
        mnuitm.addActionListener(new ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
        	if (popupBox == null || popupBox.type != UIBoxInfo.APPTBOX)
        	    return;

        	popupBox.model.edit();
        	repaint();

            }
        });
        editmenu.add(mnuitm = new JMenuItem(Resource.getPlainResourceString("Delete")));
        mnuitm.addActionListener(new ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                if (popupBox == null)
                    return;
                else if (popupBox.type == UIBoxInfo.APPTBOX)
                {
                    popupBox.model.delete();
                    repaint();
                }
            }
        });

    };

    private Rectangle resizeBox = null;

    public void setResizeBox(double x, double y, double w, double h)
    {
        if (resizeBox == null)
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

    private Rectangle dragNewBox = null;

    public void setDragNewBox(double x, double y, double w, double h)
    {
        if (dragNewBox == null)
            dragNewBox = new Rectangle();
        dragNewBox.x = (int) x;
        dragNewBox.y = (int) y;
        dragNewBox.height = (int) h;
        dragNewBox.width = (int) w;

    }

    public void removeDragNewBox()
    {
        dragNewBox = null;
        draggedAnchor = -1;
    }

    public void addBox(BoxModel bm, double x, double y, double w, double h, int boxnum)
    {
        UIBoxInfo b = new UIBoxInfo();
        b.type = UIBoxInfo.APPTBOX;

        if (bm.isOutsideGrid())
        {
            b.x = (int) x;
            b.y = (int) y;
            b.h = (int) h;
            b.w = (int) w;
        }
        else
        {
            b.x = (int) (x + w * bm.getLeftAdjustment());
            b.y = (int) (y + h * bm.getTopAdjustment());
            b.h = (int) ((bm.getBottomAdjustment() - bm.getTopAdjustment()) * h);
            b.w = (int) ((bm.getRightAdjustment() - bm.getLeftAdjustment()) * w);
        }
        b.model = bm;
        b.boxnum = boxnum;

        boxes.add(b);
    }

    public void addDateZone(BoxModel bm, double x, double y, double w, double h)
    {
        UIBoxInfo b = new UIBoxInfo();
        b.type = UIBoxInfo.DATEZONE;
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
        ImageIcon todoIcon = null;
        String iconname = Prefs.getPref(PrefName.UCS_MARKER);
        String use_marker = Prefs.getPref(PrefName.UCS_MARKTODO);
        if(use_marker.equals("true") && (iconname.endsWith(".gif") || iconname.endsWith(".jpg")))
        {
            todoIcon = new javax.swing.ImageIcon(getClass().getResource("/resource/" + iconname));         
        }
        while (it.hasNext())
        {
            UIBoxInfo b = (UIBoxInfo) it.next();
            if (b.type == UIBoxInfo.DATEZONE)
                continue;

            // add a single appt text
            // if the appt falls outside the grid - leave it as
            // a note on top
            if (b.model.isOutsideGrid())
            {

                // appt is note or is outside timespan shown
                g2.clipRect(b.x, 0, b.w+1, 1000);

                if (b.model.getTextColor().equals("strike"))
                {

                    AttributedString as = new AttributedString(b.model.getText(), stmap);
                    g2.drawString(as.getIterator(), b.x + 2, b.y + smfontHeight);
                }
                else
                {
//                  change color for a single appointment based on
                    // its color - only if color print option set
                    g2.setColor(Color.black);
                    if (b.model.getTextColor().equals("red"))
                        g2.setColor(new Color(204,0,51));
                    else if (b.model.getTextColor().equals("green"))
                        g2.setColor(new Color(0,153,0));
                    else if (b.model.getTextColor().equals("blue"))
                        g2.setColor(new Color(102,0,204));
                    // g2.setFont(sm_font);
                    if( b.model.isTodo() && todoIcon != null )
                    {
                	todoIcon.paintIcon(this, g2, b.x, b.y + 8);
                	g2.drawString(b.model.getText(), b.x + todoIcon.getIconWidth(), b.y + smfontHeight);
                    }
                    else
                    {
                	g2.drawString(b.model.getText(), b.x + 2, b.y + smfontHeight);
                    }
                    g2.setColor(Color.black);
                }

                if (b.model.isSelected())
                {
                    g2.setStroke(highlight);
                    g2.setColor(Color.BLUE);
                    g2.drawRect(b.x, b.y + 2, b.w, b.h);
                    g2.setStroke(stroke);
                }
                g2.setColor(Color.BLACK);
            }
            else
            {

                // fill the box with color
                g2.setColor(getBoxColor(b.boxnum));
                //g2.fill3DRect(b.x, b.y, b.w, b.h, true);
                g2.fillRoundRect(b.x+(int)hlthickness, b.y+(int)hlthickness, b.w-(int)hlthickness, b.h-(int)hlthickness, radius*radius, radius*radius);
                g2.setColor(getBorderColor(b.boxnum));
                g2.setStroke(highlight);
                if( b.model.isSelected())
                {
                    g2.setColor(Color.CYAN);
                }
                g2.drawRoundRect(b.x+(int)hlthickness, b.y+(int)hlthickness, b.w-(int)hlthickness, b.h-(int)hlthickness, radius*radius, radius*radius);
                g2.setStroke(stroke);

               
                // draw the appt text
                g2.setColor(Color.BLACK);
                g2.clipRect(b.x, b.y, b.w, b.h);

                // add a single appt text

                // change color for a single appointment based on
                // its color - only if color print option set
                g2.setColor(Color.black);
                if (b.model.getTextColor().equals("red"))
                    g2.setColor(new Color(204,0,51));
                else if (b.model.getTextColor().equals("green"))
                    g2.setColor(new Color(0,153,0));
                else if (b.model.getTextColor().equals("blue"))
                    g2.setColor(new Color(102,0,204));
                if( b.model.isTodo() && todoIcon != null )
                {
                    todoIcon.paintIcon(this, g2, b.x+radius, b.y+radius+8);
                    drawWrappedString(g2, b.model.getText(), b.x+radius + todoIcon.getIconWidth(), b.y+radius, b.w-radius);
                }
                else
                {
                    drawWrappedString(g2, b.model.getText(), b.x+radius, b.y+radius, b.w-radius);
                }
                

            }
            g2.setClip(s);
            g2.setColor(Color.black);
        }

        if (resizeBox != null)
        {
            g2.setStroke(highlight);
            g2.setColor(Color.RED);
            //g2.drawRect(resizeBox.x, resizeBox.y, resizeBox.width, resizeBox.height);
            g2.drawRoundRect(resizeBox.x, resizeBox.y, resizeBox.width, resizeBox.height, radius*radius, radius*radius);
            g2.setStroke(stroke);
            double top = (resizeBox.y - resizeMin) / (resizeMax - resizeMin);
            double bot = (resizeBox.y - resizeMin + resizeBox.height) / (resizeMax - resizeMin);
            g2.setColor(new Color(50,50,50));
            Rectangle2D bb = g2.getFont().getStringBounds("00:00", g2.getFontRenderContext());
            g2.fillRect(resizeBox.x+2,resizeBox.y - (int) bb.getHeight(), (int)bb.getWidth(), (int)bb.getHeight() );
            g2.fillRect(resizeBox.x+2,resizeBox.y + resizeBox.height - (int) bb.getHeight(), (int)bb.getWidth(), (int)bb.getHeight() );
            g2.setColor(Color.WHITE);
            g2.drawString(draggedBox.model.getTimeString(top), resizeBox.x+2, resizeBox.y -2 ); 
            g2.drawString(draggedBox.model.getTimeString(bot), resizeBox.x+2, resizeBox.y + resizeBox.height -2 );      
       
        }
        if (dragNewBox != null)
        {
            g2.setStroke(thicker);
            g2.setColor(Color.GREEN);
            //g2.drawRect(dragNewBox.x, dragNewBox.y, dragNewBox.width, dragNewBox.height);
            g2.drawRoundRect(dragNewBox.x, dragNewBox.y, dragNewBox.width, dragNewBox.height,radius*radius, radius*radius);
            g2.setStroke(stroke);
            double top = (dragNewBox.y - resizeMin) / (resizeMax - resizeMin);
            double bot = (dragNewBox.y - resizeMin + dragNewBox.height) / (resizeMax - resizeMin);
            g2.setColor(new Color(50,50,50));
            Rectangle2D bb = g2.getFont().getStringBounds("00:00", g2.getFontRenderContext());
            g2.fillRect(dragNewBox.x+2,dragNewBox.y - (int) bb.getHeight(), (int)bb.getWidth(), (int)bb.getHeight() );
            g2.fillRect(dragNewBox.x+2,dragNewBox.y + dragNewBox.height - (int) bb.getHeight(), (int)bb.getWidth(), (int)bb.getHeight() );
            g2.setColor(Color.WHITE);
            g2.drawString(draggedZone.model.getTimeString(top), dragNewBox.x+2, dragNewBox.y -2 ); 
            g2.drawString(draggedZone.model.getTimeString(bot), dragNewBox.x+2, dragNewBox.y + dragNewBox.height -2 );      
            
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
        LineBreakMeasurer lbm = new LineBreakMeasurer(para, new FontRenderContext(null, false, false));
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

    private ClickedBoxInfo getClickedBoxInfo(MouseEvent evt)
    {
        //			 determine which box is selected, if any
        Iterator it = boxes.iterator();
        UIBoxInfo boxFound = null;
        UIBoxInfo zone = null;
        boolean onTopBorder = false;
        boolean onBottomBorder = false;
        
        while (it.hasNext())
        {

            UIBoxInfo b = (UIBoxInfo) it.next();

            

            // System.out.println(b.layout.getAppt().getText() + " " + realx
            // + " " + realy + " " + realw + " " + realh);
            if (evt.getX() > b.x && evt.getX() < (b.x + b.w) && evt.getY() > b.y && evt.getY() < (b.y + b.h))
            {

                if (b.type == UIBoxInfo.DATEZONE)
                {
                    zone = b;
                    continue;
                }

                b.model.setSelected(true);
                boxFound = b;
                if (Math.abs(evt.getY() - b.y) < 4)
                {
                    onTopBorder = true;
                }
                else if (Math.abs(evt.getY() - (b.y + b.h)) < 4)
                {
                    onBottomBorder = true;
                }
            }
            else if (b.type == UIBoxInfo.APPTBOX)
            {
                b.model.setSelected(false);
            }
        }

        
        ClickedBoxInfo ret = new ClickedBoxInfo();
        ret.inDragNewBox = false;
        if (dragNewBox != null && evt.getX() > dragNewBox.x && evt.getX() < (dragNewBox.x + dragNewBox.width) && 
                evt.getY() > dragNewBox.y && evt.getY() < (dragNewBox.y + dragNewBox.height))
            ret.inDragNewBox = true;
        
        if( zone != null )
            ret.zone = zone;
        
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
    
    private Color boxcolor[] = null;
    private Color getBoxColor(int i)
    {
	if( boxcolor == null )
	{
	    boxcolor = new Color[4];
	    boxcolor[0] = new Color(255,235,235);
	    boxcolor[1] = new Color(235,235,255);
	    boxcolor[3] = new Color(255,255,235);
	    boxcolor[2] = new Color(235,255,235);
	}
	
	return boxcolor[i % boxcolor.length];
    }
    
    private Color bordercolor[] = null;
    private Color getBorderColor(int i)
    {
	if( bordercolor == null )
	{
	    bordercolor = new Color[4];
	    bordercolor[0] = new Color(255,100,100);
	    bordercolor[1] = new Color(100,100,255);
	    bordercolor[3] = new Color(255,255,100);
	    bordercolor[2] = new Color(100,255,100);
	}
	
	return bordercolor[i % bordercolor.length];
    }
    

}

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

import net.sf.borg.model.Appointment;

public class ApptBoxPanel extends JPanel
{

    public static final double prev_scale = 1.5; // common preview scale

    // factor

    final static private BasicStroke highlight = new BasicStroke(2.0f);

    final static private BasicStroke regular = new BasicStroke(1.0f);

    private class MyMouseListener implements MouseListener, MouseMotionListener
    {

        public MyMouseListener()
        {

        }

        public void mouseClicked(MouseEvent evt)
        {

            //if (evt.getClickCount() < 2)
            //return;

            BoxInfo b = getBox(evt);
            if (b == null)
            {
                // nothing
            }
            else if (b.box.zone)
            {
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(b.box.date);
                AppointmentListView ag = new AppointmentListView(cal
                        .get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
                        .get(Calendar.DATE));
                ag.setVisible(true);
            }
            else
            {
                Appointment ap = b.box.layout.getAppt();
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(ap.getDate());
                AppointmentListView ag = new AppointmentListView(cal
                        .get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
                        .get(Calendar.DATE));
                
                ag.showApp(ap.getKey());
                ag.setVisible(true);
                
            }

            evt.getComponent().getParent().repaint();
        }

        public void mousePressed(MouseEvent arg0)
        {
        }

        public void mouseReleased(MouseEvent arg0)
        {
        }

        public void mouseEntered(MouseEvent arg0)
        {
        }

        public void mouseExited(MouseEvent arg0)
        {
        }

        public void mouseDragged(MouseEvent arg0)
        {
            // TODO Auto-generated method stub

        }

        private class BoxInfo
        {
            public Box box;

            public boolean onBorder;
        }

        private BoxInfo getBox(MouseEvent evt)
        {
            //			 determine which box is selected, if any
            Iterator it = boxes.iterator();
            Box boxFound = null;
            Box zone = null;
            boolean onBorder = false;
            while (it.hasNext())
            {

                Box b = (Box) it.next();

                // this is bad magic
                int realx = (int) ((b.x + 10) * prev_scale);
                int realy = (int) ((b.y + 10) * prev_scale);
                int realh = (int) (b.h * prev_scale);
                int realw = (int) (b.w * prev_scale);

                // System.out.println(b.layout.getAppt().getText() + " " + realx
                // + " " + realy + " " + realw + " " + realh);
                if (evt.getX() > realx && evt.getX() < (realx + realw)
                        && evt.getY() > realy && evt.getY() < (realy + realh))
                {

                    if (b.zone == true)
                    {
                        zone = b;
                        continue;
                    }

                    b.layout.setSelected(true);
                    boxFound = b;
                    if (Math.abs(evt.getY() - realy) < 3
                            || Math.abs(evt.getY() - (realy + realh)) < 3)
                    {
                        onBorder = true;
                    }
                }
                else if (b.zone == false)
                {
                    b.layout.setSelected(false);
                }
            }

            BoxInfo ret = new BoxInfo();
            if (boxFound != null)
                ret.box = boxFound;
            else if (zone != null)
                ret.box = zone;
            else
                return null;

            ret.onBorder = onBorder;

            return ret;

        }

        public void mouseMoved(MouseEvent evt)
        {

            JPanel panel = (JPanel) evt.getComponent();

            BoxInfo b = getBox(evt);
            if (b != null && !b.box.zone)
            {
                panel.setToolTipText(b.box.layout.getAppt().getText());
            }

            /*
             if ( b.onBorder ) {
             panel.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
             } else if(b != null && !b.box.zone ){
             panel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
             } else {
             panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
             }*/

            evt.getComponent().getParent().repaint();

        }
    }

    private class Box
    {
        public int w;

        public int h;

        public int x;

        public int y;

        public ApptDayBoxLayout.ApptDayBox layout;

        public Color color;

        public boolean zone = false;

        public Date date;

    }

    private Collection boxes = new ArrayList();

    public ApptBoxPanel()
    {
        addMouseListener(new MyMouseListener());
        addMouseMotionListener(new MyMouseListener());
    };

    public void addBox(ApptDayBoxLayout.ApptDayBox layout, double x, double y,
            double w, double h, Color c, Date d)
    {
        Box b = new Box();
        b.zone = false;
        b.date = d;

        if (layout.isOutsideGrid())
        {
            b.x = (int) x;
            b.y = (int) y;
            b.h = (int) h;
            b.w = (int) w;
        }
        else
        {
            b.x = (int) (x + 2 + w * layout.getLeft());
            b.y = (int) (y + h * layout.getTop());
            b.h = (int) ((layout.getBottom() - layout.getTop()) * h);
            b.w = (int) ((layout.getRight() - layout.getLeft()) * w);
        }
        b.layout = layout;
        b.color = c;

        boxes.add(b);
    }

    public void addDateZone(Date d, double x, double y, double w, double h)
    {
        Box b = new Box();
        b.zone = true;
        b.date = d;
        b.x = (int) x;
        b.y = (int) y;
        b.h = (int) h;
        b.w = (int) w;

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
            if (b.zone == true)
                continue;

            Appointment ai = null;
            ai = b.layout.getAppt();

            // add a single appt text
            // if the appt falls outside the grid - leave it as
            // a note on top
            if (b.layout.isOutsideGrid())
            {

                // appt is note or is outside timespan shown
                g2.clipRect(b.x, 0, b.w, 1000);

                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(b.date);
                if ((ai.getColor() != null && ai.getColor().equals("strike"))
                        || (ai.getTodo() && !(ai.getNextTodo() == null || !ai
                                .getNextTodo().after(cal.getTime()))))
                {
                    // g2.setFont(strike_font);
                    // System.out.println(ai.getText());
                    // need to use AttributedString to work
                    // around a bug
                    AttributedString as = new AttributedString(ai.getText(),
                            stmap);
                    g2
                            .drawString(as.getIterator(), b.x + 2, b.y
                                    + smfontHeight);
                }
                else
                {
                    // g2.setFont(sm_font);
                    g2.drawString(ai.getText(), b.x + 2, b.y + smfontHeight);
                }

                if (b.layout.isSelected())
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

                if (b.layout.isSelected())
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
                if (ai.getColor().equals("red"))
                    g2.setColor(Color.red);
                else if (ai.getColor().equals("green"))
                    g2.setColor(Color.green);
                else if (ai.getColor().equals("blue"))
                    g2.setColor(Color.blue);

                drawWrappedString(g2, ai.getText(), b.x, b.y, b.w);

            }
            g2.setClip(s);
            g2.setColor(Color.black);
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
    // mouse click callback

}

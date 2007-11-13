package net.sf.borg.ui.calendar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.beans.Appointment;

public abstract class ApptBoxPanel extends JPanel {

    private class ClickedBoxInfo {
	public Box box;

	public boolean inDragNewBox;

	public boolean onBottomBorder;

	public boolean onTopBorder;

	public DateZone zone;
    }

    private class MyMouseListener implements MouseListener, MouseMotionListener {

	private boolean isMove = false;

	private boolean resizeTop = true;

	public MyMouseListener() {

	}

	public void mouseClicked(MouseEvent evt) {

	    evt.translatePoint(translation, translation);
	    ClickedBoxInfo b = getClickedBoxInfo(evt);
	    if (evt.getButton() == MouseEvent.BUTTON3) {
		
		if (b == null)
		    return;
		
		if (b.zone != null && b.inDragNewBox ) {
		    addmenu.show(evt.getComponent(), evt.getX(), evt.getY());
		} else if(b.box != null ){
		    if( b.box.getMenu() != null ){
			b.box.getMenu().show(evt.getComponent(), evt.getX(), evt.getY());
		    }
		}else if (b.zone != null ) {
		    b.zone.getMenu().show(evt.getComponent(), evt.getX(), evt.getY());
		}
		
		return;
	    }

	    evt.getComponent().repaint(); // to remove drag box if
	   
	    // needed

	    if (evt.getClickCount() < 2)
		return;

	  
	    if (b == null ) return;
	    else if( b.box != null ) b.box.edit();
	    else if( b.zone != null ) b.zone.edit();

	}

	public void mouseDragged(MouseEvent evt) {
	    evt.translatePoint(translation, translation);

	    if (evt.getButton() == MouseEvent.BUTTON3)
		return;

	    dragStarted = true;
	    if (draggedBox != null) {
		if (isMove == true) {
		    int top = evt.getY() - (draggedBox.getBounds().height / 2);
		    if (top < resizeMin)
			top = (int) resizeMin;
		    if (top + draggedBox.getBounds().height > resizeMax)
			top = (int) resizeMax - draggedBox.getBounds().height;

		    setResizeBox(evt.getX(), top, draggedBox.getBounds().width, draggedBox.getBounds().height);
		    // setResizeBox(draggedBox.getBounds().x, top,
		    // draggedBox.getBounds().width,
		    // draggedBox.getBounds().height);
		} else if (resizeTop == true) {
		    int top = (int) Math.max(evt.getY(), resizeMin);
		    setResizeBox(draggedBox.getBounds().x, top, draggedBox.getBounds().width, draggedBox.getBounds().height
			    + draggedBox.getBounds().y - top);
		} else {
		    int bot = (int) Math.min(evt.getY(), resizeMax);
		    setResizeBox(draggedBox.getBounds().x, draggedBox.getBounds().y, draggedBox.getBounds().width, bot
			    - draggedBox.getBounds().y);
		}
		evt.getComponent().repaint();
	    } else if (draggedAnchor != -1) {
		if (dragNewBox == null)
		    setDragNewBox(draggedZone.getBounds().x, evt.getY(), draggedZone.getBounds().width, 5);
		double y = evt.getY();
		y = Math.max(y, resizeMin);
		y = Math.min(y, resizeMax);
		if (y > draggedAnchor) {
		    setDragNewBox(dragNewBox.x, dragNewBox.y, dragNewBox.width, y - draggedAnchor);
		} else {
		    setDragNewBox(dragNewBox.x, y, dragNewBox.width, draggedAnchor - y);
		}
		evt.getComponent().repaint();
	    }
	}

	public void mouseEntered(MouseEvent evt) {
	}

	public void mouseExited(MouseEvent evt) {
	}

	public void mouseMoved(MouseEvent evt) {
	    evt.translatePoint(translation, translation);

	    if (evt.getButton() == MouseEvent.BUTTON3)
		return;

	    JPanel panel = (JPanel) evt.getComponent();

	    ClickedBoxInfo b = getClickedBoxInfo(evt);
	    if (b != null && !b.inDragNewBox) {
		if( b.box != null )
		{
		    panel.setToolTipText(b.box.getText());
		}
	    }

	    if (b != null && (b.onTopBorder || b.onBottomBorder) && !b.inDragNewBox ) {
		panel.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
	    } else if (b != null && b.box != null && !b.inDragNewBox && b.box instanceof ApptBox ) {
		panel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
	    } else {
		panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
	    }

	    evt.getComponent().repaint();

	}

	public void mousePressed(MouseEvent evt) {

	    evt.translatePoint(translation, translation);

	    if (evt.getButton() == MouseEvent.BUTTON3)
		return;

	    removeDragNewBox();
	    dragStarted = false;
	    ClickedBoxInfo b = getClickedBoxInfo(evt);
	    if (b != null &&  (b.onTopBorder || b.onBottomBorder)) {
		draggedBox = (ApptBox) b.box;
		setResizeBox(b.box.getBounds().x, b.box.getBounds().y, b.box.getBounds().width, b.box.getBounds().height);
		if (b.onBottomBorder) {
		    resizeTop = false;
		} else {
		    resizeTop = true;
		}
		isMove = false;
		evt.getComponent().repaint();
	    } else if (b != null && b.box != null && b.box instanceof ApptBox) {
		isMove = true;
		draggedBox = (ApptBox) b.box;
		setResizeBox(b.box.getBounds().x, b.box.getBounds().y, b.box.getBounds().width, b.box.getBounds().height);
		evt.getComponent().repaint();
	    } else if (b != null && b.zone != null && evt.getY() > resizeMin && evt.getY() < resizeMax) {
		// b is a date zone
		draggedZone = b.zone;
		// setDragNewBox(b.box.getBounds().x, arg0.getY(),
		// b.box.getBounds().width, 2);
		draggedAnchor = evt.getY();
	    }
	}

	public void mouseReleased(MouseEvent evt) {
	    evt.translatePoint(translation, translation);

	    if (evt.getButton() == MouseEvent.BUTTON3)
		return;
	    if (draggedBox != null && !isMove && dragStarted) {
		double y = evt.getY();
		y = Math.max(y, resizeMin);
		y = Math.min(y, resizeMax);
		try {
		    draggedBox.resize(resizeTop, (y - resizeMin) / (resizeMax - resizeMin));
		} catch (Exception e) {
		    Errmsg.errmsg(e);
		}

	    } else if (draggedBox != null && isMove && dragStarted) {
		double y = resizeBox.y;
		y = Math.max(y, resizeMin);
		y = Math.min(y, resizeMax);

		double centerx = evt.getX() + draggedBox.getBounds().width / 2;
		Date d = getDateForX(centerx);
		try {
		    draggedBox.move((y - resizeMin) / (resizeMax - resizeMin), d);
		} catch (Exception e) {
		    Errmsg.errmsg(e);
		}

	    }

	    draggedBox = null;
	    removeResizeBox();
	    evt.getComponent().repaint();

	}
    }
    
    final static private float hlthickness = 2.0f;

    final static private BasicStroke highlight = new BasicStroke(hlthickness);

    public static int realMins(double y_fraction, double startmin, double endmin) {
	double realtime = startmin + (endmin - startmin) * y_fraction;
	// round it because the double math is causing errors when later
	// converting to int
	int min = 5 * (int) Math.round(realtime / 5);
	return min;
    }

    // final static private BasicStroke regular = new BasicStroke(1.0f);
    final static private BasicStroke thicker = new BasicStroke(4.0f);

    final static private int translation = -10; // to adjust, since since

    // Graphics2D is translated

 

    private JPopupMenu addmenu = null;

 
    protected Collection boxes = new ArrayList();
    
    private int boxnum = 0;

    private int draggedAnchor = -1;

    private ApptBox draggedBox = null;

    private DateZone draggedZone = null;

    private Rectangle dragNewBox = null;

    private boolean dragStarted = false;

    private Rectangle resizeBox = null;

    private double resizeMax = 0;;

    private double resizeMin = 0;
    
    private Collection zones = new ArrayList();
    
    private class MyComponentListener implements ComponentListener
    {

	public void componentHidden(ComponentEvent arg0) {
	    // TODO Auto-generated method stub
	    
	}

	public void componentMoved(ComponentEvent e) {
	    // TODO Auto-generated method stub
	    
	}

	public void componentResized(ComponentEvent e) {
	    ApptBoxPanel p = (ApptBoxPanel) e.getComponent();
	    p.refresh();
	}

	public void componentShown(ComponentEvent e) {
	    // TODO Auto-generated method stub
	    
	}
	
    }

    public ApptBoxPanel() {
	final JPanel t = this;
	MyMouseListener myOneListener = new MyMouseListener();
	addMouseListener(myOneListener);
	addMouseMotionListener(myOneListener);
	addComponentListener(new MyComponentListener());
	JMenuItem mnuitm = null;
	addmenu = new JPopupMenu();
	addmenu.add(mnuitm = new JMenuItem(Resource.getPlainResourceString("Add_New")));
	mnuitm.addActionListener(new ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		
		    String text = JOptionPane.showInputDialog(t, Resource
			    .getPlainResourceString("Please_enter_some_appointment_text"));
		    if (text == null)
			return;
		    double top = (dragNewBox.y - resizeMin) / (resizeMax - resizeMin);
		    double bot = (dragNewBox.y - resizeMin + dragNewBox.height) / (resizeMax - resizeMin);
		    draggedZone.createAppt(top, bot, text);
		    draggedZone = null;
		    removeDragNewBox();
		    repaint();
	    }
	});


    }

    public void addApptBox(Date d,Appointment ap, double sm, double em, Rectangle bounds, Rectangle clip) {
	ApptBox b = new ApptBox(d,ap,sm,em,bounds,clip);
	


	b.setBoxnum(boxnum);
	boxnum++;

	boxes.add(b);
    }
    
    public void addNoteBox(Date d,Appointment ap, Rectangle bounds, Rectangle clip) {
	Box b = new NoteBox(d,ap,bounds,clip);
	boxes.add(b);
    }
    

    public void addDateZone(Date d, double sm, double em, Rectangle bounds) {
	DateZone b = new DateZone(d, sm, em, bounds);
	zones.add(b);
    }

    public abstract void refresh();
    
    public void clearBoxes() {
	boxes.clear();
	zones.clear();
	boxnum = 0;
    }

    public void drawBoxes(Graphics2D g2) {
	
	Font sm_font = g2.getFont();
	Map stmap = new HashMap();
	stmap.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
	stmap.put(TextAttribute.FONT, sm_font);

	Stroke stroke = g2.getStroke();
	
	Iterator it = boxes.iterator();
	while (it.hasNext()) {
	    Box b = (Box) it.next();
	    b.draw(g2,this);
	}
	

	int radius = 5;
	if (resizeBox != null) {
	    g2.setStroke(highlight);
	    g2.setColor(Color.RED);
	    g2.drawRoundRect(resizeBox.x, resizeBox.y, resizeBox.width, resizeBox.height, radius * radius, radius * radius);
	    g2.setStroke(stroke);
	    double top = (resizeBox.y - resizeMin) / (resizeMax - resizeMin);
	    double bot = (resizeBox.y - resizeMin + resizeBox.height) / (resizeMax - resizeMin);
	    g2.setColor(new Color(50, 50, 50));
	    Rectangle2D bb = g2.getFont().getStringBounds("00:00", g2.getFontRenderContext());
	    g2.fillRect(resizeBox.x + 2, resizeBox.y - (int) bb.getHeight(), (int) bb.getWidth(), (int) bb.getHeight());
	    g2.fillRect(resizeBox.x + 2, resizeBox.y + resizeBox.height - (int) bb.getHeight(), (int) bb.getWidth(), (int) bb
		    .getHeight());
	    g2.setColor(Color.WHITE);
	    g2.drawString(getTimeString(top), resizeBox.x + 2, resizeBox.y - 2);
	    g2.drawString(getTimeString(bot), resizeBox.x + 2, resizeBox.y + resizeBox.height - 2);

	}
	if (dragNewBox != null) {
	    g2.setStroke(thicker);
	    g2.setColor(Color.GREEN);
	    g2.drawRoundRect(dragNewBox.x, dragNewBox.y, dragNewBox.width, dragNewBox.height, radius * radius, radius * radius);
	    g2.setStroke(stroke);
	    double top = (dragNewBox.y - resizeMin) / (resizeMax - resizeMin);
	    double bot = (dragNewBox.y - resizeMin + dragNewBox.height) / (resizeMax - resizeMin);
	    g2.setColor(new Color(50, 50, 50));
	    Rectangle2D bb = g2.getFont().getStringBounds("00:00", g2.getFontRenderContext());
	    g2.fillRect(dragNewBox.x + 2, dragNewBox.y - (int) bb.getHeight(), (int) bb.getWidth(), (int) bb.getHeight());
	    g2.fillRect(dragNewBox.x + 2, dragNewBox.y + dragNewBox.height - (int) bb.getHeight(), (int) bb.getWidth(), (int) bb
		    .getHeight());
	    g2.setColor(Color.WHITE);
	    g2.drawString(getTimeString(top), dragNewBox.x + 2, dragNewBox.y - 2);
	    g2.drawString(getTimeString(bot), dragNewBox.x + 2, dragNewBox.y + dragNewBox.height - 2);

	}
	g2.setColor(Color.black);

    }

    public void removeDragNewBox() {
	dragNewBox = null;
	draggedAnchor = -1;
    }

    public void removeResizeBox() {
	resizeBox = null;
    }

    public void setDragNewBox(double x, double y, double w, double h) {
	if (dragNewBox == null)
	    dragNewBox = new Rectangle();
	dragNewBox.x = (int) x;
	dragNewBox.y = (int) y;
	dragNewBox.height = (int) h;
	dragNewBox.width = (int) w;

    }

    public void setResizeBounds(int min, int max) {
	resizeMin = min;
	resizeMax = max;
    }

    public void setResizeBox(double x, double y, double w, double h) {
	if (resizeBox == null)
	    resizeBox = new Rectangle();
	resizeBox.x = (int) x;
	resizeBox.y = (int) y;
	resizeBox.height = (int) h;
	resizeBox.width = (int) w;

    }


 

    private ClickedBoxInfo getClickedBoxInfo(MouseEvent evt) {
	// determine which box is selected, if any
	
	boolean onTopBorder = false;
	boolean onBottomBorder = false;
	ClickedBoxInfo ret = new ClickedBoxInfo();

	Iterator it = boxes.iterator();
	while (it.hasNext()) {

	    Box b = (Box) it.next();
	    if (evt.getX() > b.getBounds().x && evt.getX() < (b.getBounds().x + b.getBounds().width)
		    && evt.getY() > b.getBounds().y && evt.getY() < (b.getBounds().y + b.getBounds().height)) {

		b.setSelected(true);
		ret.box = b;
		if( b instanceof ApptBox)
		{
		    if (Math.abs(evt.getY() - b.getBounds().y) < 4) {
			onTopBorder = true;
		    } else if (Math.abs(evt.getY() - (b.getBounds().y + b.getBounds().height)) < 4) {
			onBottomBorder = true;
		    }
		}
	    } else {
		b.setSelected(false);
	    }
	}

	it = zones.iterator();
	while (it.hasNext()) {

	    DateZone b = (DateZone) it.next();
	    if (evt.getX() > b.getBounds().x && evt.getX() < (b.getBounds().x + b.getBounds().width)
		    && evt.getY() > b.getBounds().y && evt.getY() < (b.getBounds().y + b.getBounds().height)) {
		ret.zone = b;
		break;
	    }
	}

	
	ret.inDragNewBox = false;
	if (dragNewBox != null && evt.getX() > dragNewBox.x && evt.getX() < (dragNewBox.x + dragNewBox.width)
		&& evt.getY() > dragNewBox.y && evt.getY() < (dragNewBox.y + dragNewBox.height))
	    ret.inDragNewBox = true;

	ret.onTopBorder = onTopBorder;
	ret.onBottomBorder = onBottomBorder;

	return ret;

    }

    abstract Date getDateForX(double x);
    
    private static SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");

    public static String getTimeString(double y_fraction) {
	// start and end hour = range of Y axis
	String shr = Prefs.getPref(PrefName.WKSTARTHOUR);
	String ehr = Prefs.getPref(PrefName.WKENDHOUR);
	int startmin = 7*60;
	int endmin = 22*60;
	try {
	    startmin = Integer.parseInt(shr)*60;
	    endmin = Integer.parseInt(ehr)*60;
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}

	int realtime = ApptBoxPanel.realMins(y_fraction, startmin, endmin);
	int hour = realtime / 60;
	int min = realtime % 60;
	GregorianCalendar newCal = new GregorianCalendar();
	newCal.set(Calendar.HOUR_OF_DAY, hour);
	int roundMin = (min / 5) * 5;
	newCal.set(Calendar.MINUTE, roundMin);
	Date newTime = newCal.getTime();
	return sdf.format(newTime);
    }

}

/*
 * This file is part of BORG.
 *
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright 2005 by Mike Berger
 */
package net.sf.borg.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.Theme;
import net.sf.borg.model.sync.google.GDrive;
import net.sf.borg.ui.TrayIconProxy.TrayIconProxyI;
import net.sf.borg.ui.options.OptionsView;
import net.sf.borg.ui.popup.ReminderManager;

/**
 * A panel that displays tray-like icons with popup menus in a JFrame.
 * Mimics the behavior of SunTrayIconProxy but renders icons in a Swing panel.
 */
public class JFrameIconTray extends View implements Prefs.Listener, TrayIconProxyI{

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger("net.sf.borg");

	private static final PrefName JFRAMEICONTRAYSIZE = new PrefName("jframeIconTraySize",
			"-1,-1,60,24,N");
    private static Point mouseClickPoint;

    private JPanel iconPanel;

	private int iconSize = 16;

	private JLabel mainIconLabel;
	private JLabel syncIconLabel;
	private JLabel uploadIconLabel;

	private JPopupMenu mainMenu;
	private JPopupMenu syncMenu;
	private JPopupMenu uploadMenu;

	private JMenuItem actionMenuContainer;

	/**
	 * Constructor initializes the panel with icons and menus
	 */
	public JFrameIconTray() {
		super();
		this.setTitle("Borg Tray");

		iconPanel = new JPanel();

		 // Capture the initial click position
		iconPanel.addMouseListener(new MouseAdapter() {
            @Override
			public void mousePressed(MouseEvent e) {
                mouseClickPoint = e.getPoint();
            }
        });

        // Move the window smoothly as the user drags the mouse
		iconPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
			public void mouseDragged(MouseEvent e) {
                Point currentScreenLocation = e.getLocationOnScreen();
                setLocation(currentScreenLocation.x - mouseClickPoint.x, currentScreenLocation.y - mouseClickPoint.y);
            }
        });
		this.add(iconPanel);

		iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.X_AXIS));

		try {
			iconSize = 16; // Default size, can be adjusted
			initializeIcons();
			Prefs.addListener(this);
			startRefreshTimer();
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}


		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setUndecorated(true);
		pack();

		manageMySize(JFRAMEICONTRAYSIZE);

		setVisible(true);

	}

	/**
	 * Initialize all icons and their associated popup menus
	 */
	private void initializeIcons() throws Exception {
		// Main icon with calendar menu
		mainIconLabel = createIconLabel(
				Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/borg16.jpg")),
				"BORG");
		mainMenu = createMainMenu();
		mainIconLabel.addMouseListener(new IconMouseAdapter(mainMenu));
		iconPanel.add(mainIconLabel);

		// Sync icon
		syncIconLabel = createIconLabel(
				Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/Refresh16.gif")),
				"Sync");
		syncMenu = createSyncMenu();
		syncIconLabel.addMouseListener(new IconMouseAdapter(syncMenu));
		iconPanel.add(syncIconLabel);

		// Upload icon
		uploadIconLabel = createIconLabel(
				Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/Up16.gif")),
				"Upload");
		uploadMenu = createUploadMenu();
		uploadIconLabel.addMouseListener(new IconMouseAdapter(uploadMenu));
		iconPanel.add(uploadIconLabel);

		updateImage();
	}

	/**
	 * Create a JLabel for an icon
	 */
	private JLabel createIconLabel(Image image, String tooltip) {
		JLabel label = new JLabel();
		label.setIcon(new ImageIcon(image));
		label.setToolTipText(tooltip);
		label.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		return label;
	}

	/**
	 * Create the main calendar popup menu
	 */
	private JPopupMenu createMainMenu() {
		JPopupMenu popup = new JPopupMenu();

		String fontName = Prefs.getPref(PrefName.DEFFONT);
		if (!fontName.isEmpty()) {
			Font f = Font.decode(fontName);
			popup.setFont(f);
		}

		JMenuItem item = new JMenuItem(Resource.getResourceString("Open_Calendar"));
		item.addActionListener(e -> UIControl.toFront());
		popup.add(item);

		item = new JMenuItem(Resource.getResourceString("Show_Pops"));
		item.addActionListener(e -> {
			ReminderManager rm = ReminderManager.getReminderManager();
			if (rm != null) {
				rm.showAll();
			}
		});
		popup.add(item);

		item = new JMenuItem(Resource.getResourceString("Hide_Pops"));
		item.addActionListener(e -> {
			ReminderManager rm = ReminderManager.getReminderManager();
			if (rm != null) {
				rm.hideAll();
			}
		});
		popup.add(item);

		// Action menu container
		actionMenuContainer = new JMenuItem(Resource.getResourceString("Action"));
		popup.add(actionMenuContainer);

		item = new JMenuItem(Resource.getResourceString("Options"));
		item.addActionListener(e -> OptionsView.getReference().setVisible(true));
		popup.add(item);

		popup.addSeparator();

		item = new JMenuItem(Resource.getResourceString("About"));
		item.addActionListener(e -> MainMenu.AboutMIActionPerformed());
		popup.add(item);

		popup.addSeparator();

		item = new JMenuItem(Resource.getResourceString("Exit"));
		item.addActionListener(e -> UIControl.shutDownUI());
		popup.add(item);

		return popup;
	}

	/**
	 * Create the sync popup menu
	 */
	private JPopupMenu createSyncMenu() {
		JPopupMenu popup = new JPopupMenu();

		JMenuItem item = new JMenuItem(Resource.getResourceString("Sync"));
		item.addActionListener(SyncModule.syncButtonListener);
		popup.add(item);

		return popup;
	}

	/**
	 * Create the upload popup menu
	 */
	private JPopupMenu createUploadMenu() {
		JPopupMenu popup = new JPopupMenu();

		JMenuItem item = new JMenuItem("Upload Database");
		item.addActionListener(e -> {
			try {
				GDrive.getReference().vacuumAndUpload();
			} catch (Exception ex) {
				Errmsg.getErrorHandler().errmsg(ex);
			}
		});
		popup.add(item);

		return popup;
	}

	@Override
	public void prefsChanged() {
		updateImage();
	}

	/**
	 * Update the main icon image to show the current date or a fixed icon
	 * depending on user preference.
	 */
	@Override
	public void updateImage() {
		Image image = null;
		if (Prefs.getBoolPref(PrefName.SYSTRAYDATE)) {

			log.fine("Updating icon image...");

			// Get date text
			String text = Integer.toString(new GregorianCalendar().get(Calendar.DATE));

			BufferedImage bimage = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = bimage.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			// Draw icon background
			Theme t = Theme.getCurrentTheme();
			g.setColor(new Color(t.getTrayIconBg()));
			g.fillRect(0, 0, iconSize, iconSize);

			// Draw date centered
			Font font = Font.decode(Prefs.getPref(PrefName.TRAYFONT));
			g.setFont(font);
			FontMetrics metrics = g.getFontMetrics();
			g.setColor(new Color(t.getTrayIconFg()));
			g.drawString(text, (iconSize - metrics.stringWidth(text)) / 2,
					(iconSize + metrics.getAscent()) / 2);
			g.dispose();

			image = bimage;

			mainIconLabel
					.setToolTipText("BORG - " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date()));

		} else {
			image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/borg16.jpg"));

		}

		mainIconLabel.setIcon(new ImageIcon(image));
	}

	/**
	 * Start a timer that updates the date icon
	 */
	private void startRefreshTimer() {
		Timer updateTimer = new Timer("IconPanelTimer");
		updateTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateImage();
					}
				});
			}
		}, 60 * 1000, 15 * 60 * 1000);
	}

	/**
	 * Add an action to the main menu's action submenu
	 *
	 * @param text   the text for the menu item
	 * @param action the action listener for the menu item
	 */
	@Override
	public void addAction(String text, ActionListener action) {
		if (actionMenuContainer == null) {
			return;
		}

		JMenuItem item = new JMenuItem(text);
		item.addActionListener(action);

		// If actionMenuContainer doesn't have a submenu, create one
		if (!(actionMenuContainer instanceof javax.swing.JMenu)) {
			// Convert to JMenu for submenu support
			javax.swing.JMenu actionMenu = new javax.swing.JMenu(Resource.getResourceString("Action"));
			mainMenu.remove(actionMenuContainer);
			mainMenu.add(actionMenu, mainMenu.getComponentCount() - 3);
			actionMenuContainer = actionMenu;
		}

		if (actionMenuContainer instanceof javax.swing.JMenu) {
			((javax.swing.JMenu) actionMenuContainer).add(item);
		}
	}

	/**
	 * Enable/disable sync icon visibility
	 */
	public void setSyncIconVisible(boolean visible) {
		syncIconLabel.setVisible(visible);
	}

	/**
	 * Enable/disable upload icon visibility
	 */
	public void setUploadIconVisible(boolean visible) {
		uploadIconLabel.setVisible(visible);
	}

	/**
	 * Mouse adapter for showing popup menus on icon click
	 */
	private static class IconMouseAdapter extends MouseAdapter {
		private final JPopupMenu popup;

		IconMouseAdapter(JPopupMenu popup) {
			this.popup = popup;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
	            SafePopupShower.showSafely(popup, e.getComponent(), e.getX(), e.getY());

			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if( SwingUtilities.isLeftMouseButton(e)) {
				UIControl.toFront();
			}
		}

	
	}

	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableTrayIcon() {
		setSyncIconVisible(true);

	}

	@Override
	public void disableTrayIcon() {
		setSyncIconVisible(false);

	}

	@Override
	public void enableUploadTrayIcon() {
		setUploadIconVisible(true);
	}

	@Override
	public void disableUploadTrayIcon() {
		setUploadIconVisible(false);

	}

	@Override
	public void update(ChangeEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	}


	private class SafePopupShower {

	    public static void showSafely(JPopupMenu popup, Component invoker, int x, int y) {
	        // 1. Get the screen configuration relative to where the user clicked
	        GraphicsConfiguration config = invoker.getGraphicsConfiguration();
	        Rectangle screenBounds = config.getBounds();

	        // 2. Extract Cinnamon's precise panel height/width insets
	        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(config);

	        // Calculate the actual pixel area safe from the panel
	        int safeLeft = screenBounds.x + screenInsets.left;
	        int safeTop = screenBounds.y + screenInsets.top;
	        int safeRight = screenBounds.x + screenBounds.width - screenInsets.right;
	        int safeBottom = screenBounds.y + screenBounds.height - screenInsets.bottom;

	        // 3. Convert the click coordinates into raw screen coordinates
	        Point clickPoint = new Point(x, y);
	        SwingUtilities.convertPointToScreen(clickPoint, invoker);

	        Dimension popupSize = popup.getPreferredSize();

	        // 4. Adjust horizontal position if it clips a side panel
	        if (clickPoint.x + popupSize.width > safeRight) {
	            clickPoint.x = safeRight - popupSize.width;
	        }
	        if (clickPoint.x < safeLeft) {
	            clickPoint.x = safeLeft;
	        }

	        // 5. Adjust vertical position if it clips the bottom taskbar panel
	        if (clickPoint.y + popupSize.height > safeBottom) {
	            // Push the menu upward so it sits perfectly above the panel
	            clickPoint.y = safeBottom - popupSize.height;
	        }
	        if (clickPoint.y < safeTop) {
	            clickPoint.y = safeTop;
	        }

	        // 6. Convert the calculated safe coordinates back to component space
	        SwingUtilities.convertPointFromScreen(clickPoint, invoker);

	        // 7. Render the popup in the verified safe zone
	        popup.show(invoker, clickPoint.x, clickPoint.y);
	    }
	}

}
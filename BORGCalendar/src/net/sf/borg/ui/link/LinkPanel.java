/*
This file is part of BORG.

    BORG is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    BORG is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BORG; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

Copyright 2003 by Mike Berger
 */

package net.sf.borg.ui.link;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.LinkModel;
import net.sf.borg.model.Model;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.LinkModel.LinkType;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.entity.Address;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.KeyedEntity;
import net.sf.borg.model.entity.Link;
import net.sf.borg.model.entity.Memo;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Task;
import net.sf.borg.ui.EntitySelector;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.address.AddressView;
import net.sf.borg.ui.calendar.AppointmentListView;
import net.sf.borg.ui.memo.MemoPanel;
import net.sf.borg.ui.task.ProjectView;
import net.sf.borg.ui.task.TaskView;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.PopupMenuHelper;
import net.sf.borg.ui.util.TableSorter;

/**
 * Panel for managing links. This panel is included in the UIs for entities that
 * have links.
 */
public class LinkPanel extends JPanel implements Model.Listener {

	private static final long serialVersionUID = 1L;

	/**
	 * Provide a human readable name for a link
	 * 
	 * @param at
	 *            the link
	 * @return the name
	 * @throws Exception
	 */
	public static String getName(Link at) throws Exception {
		if (at.getLinkType().equals(LinkType.URL.toString()))
			return at.getPath();
		else if (at.getLinkType().equals(LinkType.APPOINTMENT.toString())) {
			Appointment ap = AppointmentModel.getReference().getAppt(
					Integer.parseInt(at.getPath()));
			if (ap != null) {
				return (Resource.getResourceString("appointment") + "["
						+ ap.getText() + "]");
			}
		} else if (at.getLinkType().equals(LinkType.PROJECT.toString())) {
			Project ap = TaskModel.getReference().getProject(
					Integer.parseInt(at.getPath()));
			if (ap != null) {
				return (Resource.getResourceString("project") + "["
						+ ap.getDescription() + "]");
			}
		} else if (at.getLinkType().equals(LinkType.TASK.toString())) {
			Task ap = TaskModel.getReference().getTask(
					Integer.parseInt(at.getPath()));
			if (ap != null) {
				return (Resource.getResourceString("task") + "["
						+ ap.getDescription() + "]");
			}
		} else if (at.getLinkType().equals(LinkType.ADDRESS.toString())) {
			Address ap = AddressModel.getReference().getAddress(
					Integer.parseInt(at.getPath()));
			if (ap != null) {
				return (Resource.getResourceString("Address") + "["
						+ ap.getLastName() + "," + ap.getFirstName() + "]");
			}
		} else if (at.getLinkType().equals(LinkType.MEMO.toString())) {
			return (Resource.getResourceString("memo") + "[" + at.getPath() + "]");
		} else if (at.getLinkType().equals(LinkType.FILELINK.toString())) {
			File f = new File(at.getPath());
			return f.getName();
		} else if (at.getLinkType().equals(LinkType.ATTACHMENT.toString())) {
			return at.getPath();
		}

		return "error";
	}

	/** the entity that owns the links */
	private KeyedEntity<?> owningEntity;

	/** table to show the links */
	private JTable linkTable = new JTable();

	/**
	 * constructor
	 */
	public LinkPanel() {
		initComponents();

		// listen for link model changes
		LinkModel.getReference().addListener(this);
	}

	/**
	 * indicate if the entity shown by this panel has any links
	 * 
	 * @return true if the owning entity has links
	 */
	public boolean hasLinks() {

		try {
			Collection<Link> atts = LinkModel.getReference().getLinks(
					owningEntity);
			if (!atts.isEmpty())
				return true;
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		return false;
	}

	/**
	 * initialize the UI
	 */
	public void initComponents() {

		this.setLayout(new GridBagLayout());

		linkTable
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// table model will contain link name and key.
		linkTable.setModel(new TableSorter(new String[] { "", "Key" },
				new Class[] { java.lang.String.class, Integer.class }));

		// set up for sorting when a column header is clicked
		TableSorter tm = (TableSorter) linkTable.getModel();
		tm.addMouseListenerToHeaderInTable(linkTable);

		// set column widths
		linkTable.getColumnModel().getColumn(0).setPreferredWidth(125);
		linkTable.getColumnModel().getColumn(1).setPreferredWidth(75);

		linkTable.setPreferredScrollableViewportSize(new Dimension(150, 100));

		// on double-click - open the object referenced by the link
		linkTable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				if (evt.getClickCount() < 2)
					return;
				openSelected();
			}
		});

		// add a popup menu to the link table
		new PopupMenuHelper(linkTable, new PopupMenuHelper.Entry[] {
		// open menu item
				new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						openSelected();
					}
				}, "Open"),
				// delete menu item
				new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						TableSorter ts = (TableSorter) linkTable.getModel();
						int[] indices = linkTable.getSelectedRows();

						for (int i = 0; i < indices.length; ++i) {
							int index = indices[i];
							try {
								Integer key = (Integer) ts.getValueAt(index, 1);
								LinkModel.getReference().delete(key.intValue());

							} catch (Exception e) {
								Errmsg.errmsg(e);
							}
						}

					}
				}, "Delete"),
				// show link properties menu item
				new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						TableSorter ts = (TableSorter) linkTable.getModel();
						int[] indices = linkTable.getSelectedRows();
						// show link type and path
						for (int i = 0; i < indices.length; ++i) {
							int index = indices[i];
							try {
								Integer key = (Integer) ts.getValueAt(index, 1);
								Link l = LinkModel.getReference().getLink(
										key.intValue());
								String info = Resource
										.getResourceString("Type")
										+ ":"
										+ l.getLinkType()
										+ "\n\n["
										+ l.getPath() + "]";
								Errmsg.notice(info);
							} catch (Exception e) {
								Errmsg.errmsg(e);
							}
						}

					}
				}, "Properties") });

		// hide the key column from view
		TableColumnModel tcm = linkTable.getColumnModel();
		TableColumn column = tcm.getColumn(1);
		tcm.removeColumn(column);

		// add a scroll to the table
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setViewportView(linkTable);
		this.add(scrollPanel, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));

		// add the link file button
		JButton linkFilebutton = new JButton(Resource
				.getResourceString("link_file"));
		linkFilebutton.setIcon(new javax.swing.ImageIcon(getClass()
				.getResource("/resource/Add16.gif")));
		linkFilebutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				if (owningEntity == null || owningEntity.getKey() == -1) {
					Errmsg.notice(Resource.getResourceString("att_owner_null"));
					return;
				}

				// prompt for a file and create a link with the file's absolute
				// path
				File file;
				while (true) {
					// prompt for a file
					JFileChooser chooser = new JFileChooser();
					chooser.setCurrentDirectory(new File("."));
					chooser.setDialogTitle(Resource
							.getResourceString("choose_file"));
					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					int returnVal = chooser.showOpenDialog(null);
					if (returnVal != JFileChooser.APPROVE_OPTION)
						return;
					String s = chooser.getSelectedFile().getAbsolutePath();
					file = new File(s);
					break;
				}

				try {
					LinkModel.getReference().addLink(owningEntity,
							file.getAbsolutePath(), LinkType.FILELINK);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});

		// attach file button
		JButton attachFileButton = new JButton(Resource
				.getResourceString("attach_file"));
		attachFileButton.setIcon(new javax.swing.ImageIcon(getClass()
				.getResource("/resource/Copy16.gif")));
		attachFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (owningEntity == null || owningEntity.getKey() == -1) {
					Errmsg.notice(Resource.getResourceString("att_owner_null"));
					return;
				}

				// prompt or a file and add an attachment link
				// the model will copy the file to the attachement folder
				File file;
				while (true) {
					// prompt for a file
					JFileChooser chooser = new JFileChooser();
					chooser.setCurrentDirectory(new File("."));
					chooser.setDialogTitle(Resource
							.getResourceString("choose_file"));
					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					int returnVal = chooser.showOpenDialog(null);
					if (returnVal != JFileChooser.APPROVE_OPTION)
						return;
					String s = chooser.getSelectedFile().getAbsolutePath();
					file = new File(s);
					break;
				}

				try {

					LinkModel.getReference().addLink(owningEntity,
							file.getAbsolutePath(), LinkType.ATTACHMENT);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});

		// URL link button
		JButton urlLinkButton = new JButton(Resource.getResourceString("url"));
		urlLinkButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/WebComponent16.gif")));
		urlLinkButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				if (owningEntity == null || owningEntity.getKey() == -1) {
					Errmsg.notice(Resource.getResourceString("att_owner_null"));
					return;
				}

				// prompt for a url and save as a link
				// no validation is done
				String url = JOptionPane.showInputDialog(Resource
						.getResourceString("url")
						+ "?");
				if (url == null)
					return;

				try {
					LinkModel.getReference().addLink(owningEntity, url,
							LinkType.URL);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});

		// borg entity link button
		JButton entityLinkbutton = new JButton("BORG");
		entityLinkbutton.setIcon(new javax.swing.ImageIcon(getClass()
				.getResource("/resource/borg16.jpg")));
		entityLinkbutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				if (owningEntity == null || owningEntity.getKey() == -1) {
					Errmsg.notice(Resource.getResourceString("att_owner_null"));
					return;
				}

				// prompt for the entity type
				Object[] possibleValues = {
						Resource.getResourceString("appointment"),
						Resource.getResourceString("task"),
						Resource.getResourceString("memo"),
						Resource.getResourceString("Address"),
						Resource.getResourceString("project") };
				String selectedValue = (String) JOptionPane.showInputDialog(
						null, Resource.getResourceString("select_link_type"),
						"BORG", JOptionPane.INFORMATION_MESSAGE, null,
						possibleValues, possibleValues[0]);
				if (selectedValue == null)
					return;
				try {

					// for each entity type, bring up a dialog to select the
					// entity instance and
					// then add the link
					if (selectedValue.equals(Resource
							.getResourceString("appointment"))) {
						Appointment ap = EntitySelector.selectAppointment();
						if (ap != null) {
							LinkModel.getReference().addLink(owningEntity,
									Integer.toString(ap.getKey()),
									LinkType.APPOINTMENT);
						}
					} else if (selectedValue.equals(Resource
							.getResourceString("project"))) {
						Project ap = EntitySelector.selectProject();
						if (ap != null) {
							LinkModel.getReference().addLink(owningEntity,
									Integer.toString(ap.getKey()),
									LinkType.PROJECT);
						}
					} else if (selectedValue.equals(Resource
							.getResourceString("task"))) {
						Task ap = EntitySelector.selectTask();
						if (ap != null) {
							LinkModel.getReference().addLink(owningEntity,
									Integer.toString(ap.getKey()),
									LinkType.TASK);
						}
					} else if (selectedValue.equals(Resource
							.getResourceString("Address"))) {
						Address ap = EntitySelector.selectAddress();
						if (ap != null) {
							LinkModel.getReference().addLink(owningEntity,
									Integer.toString(ap.getKey()),
									LinkType.ADDRESS);
						}
					} else if (selectedValue.equals(Resource
							.getResourceString("memo"))) {
						Memo ap = EntitySelector.selectMemo();
						if (ap != null) {
							LinkModel.getReference().addLink(owningEntity,
									ap.getMemoName(), LinkType.MEMO);
						}
					}
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});

		// button panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(linkFilebutton);
		buttonPanel.add(attachFileButton);
		buttonPanel.add(urlLinkButton);
		buttonPanel.add(entityLinkbutton);
		this.add(buttonPanel, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.NONE, 1.0, 1.0));

		// if we can't figure out the attachement folder, then disable the
		// attachment button
		// attachments only work for hsql right now since we have a folder
		// readily available
		if (LinkModel.attachmentFolder() == null) {
			attachFileButton.setEnabled(false);
		}

	}

	/**
	 * get the item referenced by the link and open it. The non-entity link
	 * types depend on java.awt.Desktop to open them in the correct "native"
	 * way. If the version of Java does not support desktop, then this method is
	 * limited to only opening borg entities
	 */
	public void openSelected() {

		TableSorter ts = (TableSorter) linkTable.getModel();
		int[] indices = linkTable.getSelectedRows();

		for (int i = 0; i < indices.length; ++i) {
			int index = indices[i];
			try {
				Integer key = (Integer) ts.getValueAt(index, 1);
				Link at = LinkModel.getReference().getLink(key.intValue());

				// open a linked file
				if (at.getLinkType().equals(LinkType.FILELINK.toString())) {
					File file = new File(at.getPath());
					if (!file.exists()) {
						Errmsg.notice(Resource
								.getResourceString("att_not_found"));
						return;
					}
					if (Desktop.isDesktopSupported() == false)
						return;
					Desktop.getDesktop().open(file);
				}
				// open an attachment
				else if (at.getLinkType()
						.equals(LinkType.ATTACHMENT.toString())) {
					File file = new File(LinkModel.attachmentFolder() + "/"
							+ at.getPath());
					if (!file.exists()) {
						Errmsg.notice(Resource
								.getResourceString("att_not_found"));
						return;
					}
					if (Desktop.isDesktopSupported() == false)
						return;
					Desktop.getDesktop().open(file);
				}
				// open a url
				else if (at.getLinkType().equals(LinkType.URL.toString())) {
					if (Desktop.isDesktopSupported() == false)
						return;
					Desktop.getDesktop().browse(new URI(at.getPath()));
				}
				// open an appointment
				else if (at.getLinkType().equals(
						LinkType.APPOINTMENT.toString())) {
					Appointment ap = AppointmentModel.getReference().getAppt(
							Integer.parseInt(at.getPath()));
					if (ap == null) {
						Errmsg.notice(Resource
								.getResourceString("att_not_found"));
						return;
					}

					GregorianCalendar cal = new GregorianCalendar();
					cal.setTime(ap.getDate());

					// bring up an appt editor window
					AppointmentListView ag = new AppointmentListView(cal
							.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
							.get(Calendar.DATE));
					ag.showApp(ap.getKey());
					ag.showView();
				}
				// open a project
				else if (at.getLinkType().equals(LinkType.PROJECT.toString())) {
					Project ap = TaskModel.getReference().getProject(
							Integer.parseInt(at.getPath()));
					if (ap == null) {
						Errmsg.notice(Resource
								.getResourceString("att_not_found"));
						return;
					}

					new ProjectView(ap, ProjectView.Action.CHANGE, null)
							.showView();
				}
				// open a task
				else if (at.getLinkType().equals(LinkType.TASK.toString())) {
					Task ap = TaskModel.getReference().getTask(
							Integer.parseInt(at.getPath()));
					if (ap == null) {
						Errmsg.notice(Resource
								.getResourceString("att_not_found"));
						return;
					}
					
					new TaskView(ap, TaskView.Action.CHANGE, null).showView();
				}
				// open an address
				else if (at.getLinkType().equals(LinkType.ADDRESS.toString())) {
					Address ap = AddressModel.getReference().getAddress(
							Integer.parseInt(at.getPath()));
					if (ap == null) {
						Errmsg.notice(Resource
								.getResourceString("att_not_found"));
						return;
					}
					new AddressView(ap).showView();
				}
				// open a memo
				else if (at.getLinkType().equals(LinkType.MEMO.toString())) {
					Component c  = MultiView.getMainView().setView(ViewType.MEMO);
					
					// show the actual memo
					if( c != null && c instanceof MemoPanel)
					{
						MemoPanel mp = (MemoPanel)c;
						mp.selectMemo(at.getPath());
					}
				}
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}
		}
	}
	
	@Override
	public void update(ChangeEvent event) {
		refresh();
	}

	/**
	 * reload the data from the model and redisplay
	 */
	public void refresh() {

		TableSorter tm = (TableSorter) linkTable.getModel();
		tm.setRowCount(0);
		try {
			Collection<Link> atts = LinkModel.getReference().getLinks(
					owningEntity);
			Iterator<Link> it = atts.iterator();
			while (it.hasNext()) {
				Link at = (it.next());
				tm.addRow(new Object[] { LinkPanel.getName(at),
						new Integer(at.getKey()) });
			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * set the owning entity of this panel
	 * 
	 * @param owner
	 *            owning entity
	 */
	public void setOwner(KeyedEntity<?> owner) {

		owningEntity = owner;
		refresh();
	}

}

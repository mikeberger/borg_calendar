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
import javax.swing.JLabel;
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
import net.sf.borg.model.entity.Address;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.KeyedEntity;
import net.sf.borg.model.entity.Link;
import net.sf.borg.model.entity.Memo;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Task;
import net.sf.borg.ui.BeanSelector;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.address.AddressView;
import net.sf.borg.ui.calendar.AppointmentListView;
import net.sf.borg.ui.task.ProjectView;
import net.sf.borg.ui.task.TaskView;
import net.sf.borg.ui.util.PopupMenuHelper;
import net.sf.borg.ui.util.TableSorter;

public class LinkPanel extends JPanel implements Model.Listener {
	private KeyedEntity<?> owner_;
	private JScrollPane scrollPanel = new JScrollPane();
	private JTable table = new JTable();
	private JLabel notsupp = new JLabel(Resource.getPlainResourceString("not_supported"));
	
	public LinkPanel() {
		initComponents();
		LinkModel.getReference().addListener(this);
	}

	public void initComponents() {
		
		if( !LinkModel.getReference().hasLinks())
		{
			this.add(notsupp);
			return;
		}
		
		this.setLayout(new GridBagLayout());

		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setModel(new TableSorter(new String[] { "", "Key" }, new Class[] {
				java.lang.String.class, Integer.class }));
		// set up for sorting when a column header is clicked
		TableSorter tm = (TableSorter) table.getModel();
		tm.addMouseListenerToHeaderInTable(table);

		// set column widths
		table.getColumnModel().getColumn(0).setPreferredWidth(125);
		table.getColumnModel().getColumn(1).setPreferredWidth(75);

		table.setPreferredScrollableViewportSize(new Dimension(150, 100));

		table.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				tableMouseClicked(evt);
			}
		});

		// ListSelectionModel rowSM = table.getSelectionModel();
		// rowSM.table(this);

		new PopupMenuHelper(table, new PopupMenuHelper.Entry[] {
				new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {

						openSelected();
					}
				}, "Open"),
				new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						TableSorter ts = (TableSorter) table.getModel();
						int[] indices = table.getSelectedRows();

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
				new PopupMenuHelper.Entry(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						TableSorter ts = (TableSorter) table.getModel();
						int[] indices = table.getSelectedRows();

						for (int i = 0; i < indices.length; ++i) {
							int index = indices[i];
							try {
								Integer key = (Integer) ts.getValueAt(index, 1);
								Link l = LinkModel.getReference().getLink(key.intValue());
								String info = Resource.getPlainResourceString("Type") + ":" + l.getLinkType() +
								"\n\n[" + l.getPath() + "]";
								Errmsg.notice(info);
							} catch (Exception e) {
								Errmsg.errmsg(e);
							}
						}

					}
				}, "Properties")});

		TableColumnModel tcm = table.getColumnModel();
		TableColumn column = tcm.getColumn(1);
		tcm.removeColumn(column);

		scrollPanel.setViewportView(table);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		this.add(scrollPanel, gbc);

		JButton linkfileb = new JButton(Resource.getPlainResourceString("link_file"));
		linkfileb.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Add16.gif")));
		linkfileb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				
				if (owner_ == null || owner_.getKey() == -1) {
					Errmsg.notice(Resource
							.getPlainResourceString("att_owner_null"));
					return;
				}
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
					LinkModel.getReference().addLink(owner_,
							file.getAbsolutePath(), LinkModel.FILELINK);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});
		
		JButton attfileb = new JButton(Resource.getPlainResourceString("attach_file"));
		attfileb.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Copy16.gif")));
		attfileb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				
				if (owner_ == null || owner_.getKey() == -1) {
					Errmsg.notice(Resource
							.getPlainResourceString("att_owner_null"));
					return;
				}
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
					
					LinkModel.getReference().addLink(owner_,
							file.getAbsolutePath(), LinkModel.ATTACHMENT);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});

		JButton urlb = new JButton(Resource.getPlainResourceString("url"));
		urlb.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/WebComponent16.gif")));
		urlb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				if (owner_ == null || owner_.getKey() == -1) {
					Errmsg.notice(Resource
							.getPlainResourceString("att_owner_null"));
					return;
				}

				String url = JOptionPane.showInputDialog(Resource
						.getPlainResourceString("url")
						+ "?");
				if (url == null)
					return;

				try {
					LinkModel.getReference()
							.addLink(owner_, url, LinkModel.URL);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});
		JButton borgb = new JButton("BORG");
		borgb.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/borg16.jpg")));
		borgb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				if (owner_ == null || owner_.getKey() == -1) {
					Errmsg.notice(Resource
							.getPlainResourceString("att_owner_null"));
					return;
				}

				Object[] possibleValues = { 
						Resource.getPlainResourceString("appointment"), 
						Resource.getPlainResourceString("task"),
						Resource.getPlainResourceString("memo"),
						Resource.getPlainResourceString("Address"),
						Resource.getPlainResourceString("project")};
				String selectedValue = (String)JOptionPane.showInputDialog(null, 
						Resource.getPlainResourceString("select_link_type"), "BORG",
						JOptionPane.INFORMATION_MESSAGE, null,possibleValues, possibleValues[0]);
				if (selectedValue == null)
					return;
				try{
					if( selectedValue.equals(Resource.getPlainResourceString("appointment")))
					{
						Appointment ap = BeanSelector.selectAppointment();
						if( ap != null )
						{
							LinkModel.getReference().addLink(owner_, Integer.toString(ap.getKey()), LinkModel.APPOINTMENT);
						}
					}
					else if( selectedValue.equals(Resource.getPlainResourceString("project")))
					{
						Project ap = BeanSelector.selectProject();
						if( ap != null )
						{
							LinkModel.getReference().addLink(owner_, Integer.toString(ap.getKey()), LinkModel.PROJECT);
						}
					}
					else if( selectedValue.equals(Resource.getPlainResourceString("task")))
					{
						Task ap = BeanSelector.selectTask();
						if( ap != null )
						{
							LinkModel.getReference().addLink(owner_, Integer.toString(ap.getKey()), LinkModel.TASK);
						}
					}
					else if( selectedValue.equals(Resource.getPlainResourceString("Address")))
					{
						Address ap = BeanSelector.selectAddress();
						if( ap != null )
						{
							LinkModel.getReference().addLink(owner_, Integer.toString(ap.getKey()), LinkModel.ADDRESS);
						}
					}
					else if( selectedValue.equals(Resource.getPlainResourceString("memo")))
					{
						Memo ap = BeanSelector.selectMemo();
						if( ap != null )
						{
							LinkModel.getReference().addLink(owner_, ap.getMemoName(), LinkModel.MEMO);
						}
					}
				}
				catch(Exception e)
				{
					Errmsg.errmsg(e);
				}
			}
		});

		JPanel butPanel = new JPanel();
		butPanel.add(linkfileb);
		butPanel.add(attfileb);
		butPanel.add(urlb);
		butPanel.add(borgb);
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = 1;
		this.add(butPanel, gbc);
		
		if( LinkModel.attachmentFolder() == null)
		{
			attfileb.setEnabled(false);
		}

	}

	public void openSelected() {
		
		
		if( Desktop.isDesktopSupported() == false)
			return;
		
		TableSorter ts = (TableSorter) table.getModel();
		int[] indices = table.getSelectedRows();

		for (int i = 0; i < indices.length; ++i) {
			int index = indices[i];
			try {
				Integer key = (Integer) ts.getValueAt(index, 1);
				// String path = (String)
				// ts.getValueAt(index, 0);
				Link at = LinkModel.getReference().getLink(key.intValue());
				if (at.getLinkType().equals(LinkModel.FILELINK.toString())) {
					File file = new File(at.getPath());
					if (!file.exists()) {
						Errmsg.notice(Resource
								.getPlainResourceString("att_not_found"));
						return;
					}
					Desktop.getDesktop().open(file);
				}
				else if (at.getLinkType().equals(LinkModel.ATTACHMENT.toString())) {
					File file = new File(LinkModel.attachmentFolder() + "/" + at.getPath());
					if (!file.exists()) {
						Errmsg.notice(Resource
								.getPlainResourceString("att_not_found"));
						return;
					}
					Desktop.getDesktop().open(file);
				}
				else if (at.getLinkType().equals(LinkModel.URL.toString())) {
					Desktop.getDesktop().browse(new URI(at.getPath()));
				}
				else if (at.getLinkType().equals(LinkModel.APPOINTMENT.toString())) {
					Appointment ap = AppointmentModel.getReference().getAppt(Integer.parseInt(at.getPath()));
					if (ap == null) {
						Errmsg.notice(Resource
								.getPlainResourceString("att_not_found"));
						return;
					}
					
					GregorianCalendar cal = new GregorianCalendar();
				    cal.setTime(ap.getDate());

				    // bring up an appt editor window
				    AppointmentListView ag = new AppointmentListView(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
					    .get(Calendar.DATE));
				    ag.showApp(ap.getKey());
				    MultiView.getMainView().addView(ag);
				}
				else if (at.getLinkType().equals(LinkModel.PROJECT.toString())) {
					Project ap = TaskModel.getReference().getProject(Integer.parseInt(at.getPath()));
					if (ap == null) {
						Errmsg.notice(Resource
								.getPlainResourceString("att_not_found"));
						return;
					}
					
					 MultiView.getMainView().addView(new ProjectView(ap, ProjectView.T_CHANGE, null));
				}
				else if (at.getLinkType().equals(LinkModel.TASK.toString())) {
					Task ap = TaskModel.getReference().getTask(Integer.parseInt(at.getPath()));
					if (ap == null) {
						Errmsg.notice(Resource
								.getPlainResourceString("att_not_found"));
						return;
					}
					
					 MultiView.getMainView().addView(new TaskView(ap, TaskView.T_CHANGE, null));
				}
				else if (at.getLinkType().equals(LinkModel.ADDRESS.toString())) {
					Address ap = AddressModel.getReference().getAddress(Integer.parseInt(at.getPath()));
					if (ap == null) {
						Errmsg.notice(Resource
								.getPlainResourceString("att_not_found"));
						return;
					}
					
					 MultiView.getMainView().addView(new AddressView(ap));
				}
				else if (at.getLinkType().equals(LinkModel.MEMO.toString())) {
					
				    MultiView.getMainView().showMemos(at.getPath());
				}
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}
		}
	}

	public void refresh() {

		TableSorter tm = (TableSorter) table.getModel();
		tm.setRowCount(0);
		try {
			Collection<Link> atts = LinkModel.getReference().getLinks(owner_);
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

	public void remove() {
		// TODO Auto-generated method stub

	}
	
	public boolean hasLinks()
	{
		if( LinkModel.getReference().hasLinks())
		{
			try {
				Collection<Link> atts = LinkModel.getReference().getLinks(owner_);
				if( !atts.isEmpty() )
					return true;
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}

		}
		
		return false;
	}

	public void setOwner(KeyedEntity<?> owner) {
		if( !LinkModel.getReference().hasLinks())
		{
			owner_ = null;
			return;
		}
		owner_ = owner;
		refresh();
	}

	private void tableMouseClicked(java.awt.event.MouseEvent evt) {
		if (evt.getClickCount() < 2)
			return;
		openSelected();
	}

	public static String getName(Link at) throws Exception {
	    if (at.getLinkType().equals(LinkModel.URL.toString()))
	        return at.getPath();
	    else if (at.getLinkType().equals(LinkModel.APPOINTMENT.toString())) {
	        Appointment ap = AppointmentModel.getReference().getAppt(Integer.parseInt(at.getPath()));
	        if (ap != null) {
	            return (Resource.getPlainResourceString("appointment") + "[" + ap.getText() + "]");
	        }
	
	    } else if (at.getLinkType().equals(LinkModel.PROJECT.toString())) {
	        Project ap = TaskModel.getReference().getProject(Integer.parseInt(at.getPath()));
	        if (ap != null) {
	            return (Resource.getPlainResourceString("project") + "[" + ap.getDescription() + "]");
	        }
	
	    } else if (at.getLinkType().equals(LinkModel.TASK.toString())) {
	        Task ap = TaskModel.getReference().getTask(Integer.parseInt(at.getPath()));
	        if (ap != null) {
	            return (Resource.getPlainResourceString("task") + "[" + ap.getDescription() + "]");
	        }
	
	    } else if (at.getLinkType().equals(LinkModel.ADDRESS.toString())) {
	        Address ap = AddressModel.getReference().getAddress(Integer.parseInt(at.getPath()));
	        if (ap != null) {
	            return (Resource.getPlainResourceString("Address") + "[" + ap.getLastName() + "," + ap.getFirstName() + "]");
	        }
	
	    } else if (at.getLinkType().equals(LinkModel.MEMO.toString())) {
	        return (Resource.getPlainResourceString("memo") + "[" + at.getPath() + "]");
	    } else if (at.getLinkType().equals(LinkModel.FILELINK.toString())) {
	        File f = new File(at.getPath());
	        return f.getName();
	    } else if (at.getLinkType().equals(LinkModel.ATTACHMENT.toString())) {
	        return at.getPath();
	    }
	
	    return "error";
	}
}

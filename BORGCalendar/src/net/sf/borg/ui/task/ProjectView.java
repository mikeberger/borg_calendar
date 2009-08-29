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
package net.sf.borg.ui.task;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import net.sf.borg.common.DateUtil;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Resource;
import net.sf.borg.common.Warning;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Task;
import net.sf.borg.ui.DockableView;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.RunReport;
import net.sf.borg.ui.link.LinkPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

import com.toedter.calendar.JDateChooser;

/**
 * 
 * @author MBERGER
 * @version
 */

public class ProjectView extends DockableView {

	static int T_ADD = 2;

	public static int T_CHANGE = 3;

	// the different function values for calls to show task
	static int T_CLONE = 1;

	static public Integer getProjectId(String s) throws Exception {
		int i = s.indexOf(":");
		if (i == -1)
			throw new Exception("Cannot parse project label");
		String ss = s.substring(0, i);

		int pid = Integer.parseInt(ss);
		return new Integer(pid);

	}

	static public String getProjectString(Project p) {
		return p.getKey() + ":" + p.getDescription();
	}

	private JComboBox catbox = null;

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JLabel catlabel;

	private JLabel daysLeftLabel = null;

	private JTextField daysLeftText = null;

	private JLabel descLabel = null;

	private javax.swing.JTextField description;

	private JDateChooser duedatechooser;

	private JButton ganttbutton;

	private javax.swing.JTextField itemtext;

	private javax.swing.JButton jButton2;

	private javax.swing.JMenu jMenu1;

	private javax.swing.JMenuBar jMenuBar1;

	private javax.swing.JMenuItem jMenuItem1;

	private javax.swing.JMenuItem jMenuItem2;

	private javax.swing.JPanel jPanel3;

	private javax.swing.JPanel jPanel4;

	private javax.swing.JLabel lblDueDate;

	private javax.swing.JLabel lblItemNum;

	private javax.swing.JLabel lblStartDate;

	private javax.swing.JLabel lblStatus;

	private JLabel openLabel = null;

	private JTextField openText = null;

	private JComboBox projBox = new JComboBox();

	private JDateChooser startdatechooser;

	private javax.swing.JComboBox statebox;

	private String title_ = "";

	private JLabel totalLabel = null;

	private JTextField totalText = null;

	private LinkPanel linkPanel = new LinkPanel();

	private TaskListPanel taskPanel = null;

	private JPanel taskBorder = null;

	public ProjectView(Project p, int function, Integer parentId)
			throws Exception {
		super();
		addModel(TaskModel.getReference());

		initComponents(); // init the GUI widgets

		try {
			Collection<String> cats = CategoryModel.getReference()
					.getCategories();
			Iterator<String> it = cats.iterator();
			while (it.hasNext()) {
				catbox.addItem(it.next());
			}
			catbox.setSelectedIndex(0);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		showProject(function, p, parentId);

	}

	public PrefName getFrameSizePref() {
		return PrefName.PROJVIEWSIZE;
	}

	public String getFrameTitle() {
		return title_;
	}

	public JMenuBar getMenuForFrame() {
		ResourceHelper.setText(jMenu1, "Menu");
		ResourceHelper.setText(jMenuItem1, "Save");
		jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveProject(evt);
			}
		});

		jMenu1.add(jMenuItem1);

		ResourceHelper.setText(jMenuItem2, "Dismiss");
		jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				disact(evt);
			}
		});

		jMenu1.add(jMenuItem2);

		jMenuBar1.add(jMenu1);
		return jMenuBar1;
	}

	// the task editor currently does not refresh itself when the task data
	// model changes
	// - although it should not be changing while the task editor is open
	public void refresh() {
	}

	private void disact(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_disact
		this.remove();
	}

	private void ganttActionPerformed(java.awt.event.ActionEvent evt) {

		// get the task number from column 0 of the selected row
		String num = itemtext.getText();
		if (num.equals("NEW"))
			return;

		int pnum = Integer.parseInt(num);
		try {
			TaskModel taskmod_ = TaskModel.getReference();
			Project p = taskmod_.getProject(pnum);
			GanttFrame.showChart(p);
		} catch (ClassNotFoundException cnf) {
			Errmsg.notice(Resource.getResourceString("borg_jasp"));
		} catch (NoClassDefFoundError r) {
			Errmsg.notice(Resource.getResourceString("borg_jasp"));
		} catch (Warning w) {
			Errmsg.notice(w.getMessage());
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}
	
	


	private JComboBox getCatbox() {
		if (catbox == null) {
			catbox = new JComboBox();
		}
		return catbox;
	}

	/**
	 * This method initializes daysLeftText
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getDaysLeftText() {
		if (daysLeftText == null) {
			daysLeftText = new JTextField();
			daysLeftText.setEditable(false);
		}
		return daysLeftText;
	}

	private JButton getGanttbutton() {
		if (ganttbutton == null) {
			ganttbutton = new JButton();
			ganttbutton.setText(Resource.getResourceString("GANTT"));
			// ganttbutton.setIcon(new
			// ImageIcon(getClass().getResource("/resource/Add16.gif")));
			ganttbutton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					ganttActionPerformed(e);
				}
			});
		}
		return ganttbutton;
	}

	/**
	 * This method initializes openText
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getOpenText() {
		if (openText == null) {
			openText = new JTextField();
			openText.setEditable(false);
		}
		return openText;
	}

	/**
	 * This method initializes totalText
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getTotalText() {
		if (totalText == null) {
			totalText = new JTextField();
			totalText.setEditable(false);
		}
		return totalText;
	}

	private void initComponents()// GEN-BEGIN:initComponents
	{

	
		openLabel = new JLabel();
		openLabel.setText(Resource.getResourceString("open_tasks"));
		
		totalLabel = new JLabel();
		totalLabel.setText(Resource.getResourceString("total_tasks"));
		
		descLabel = new JLabel();
		descLabel.setText(Resource.getResourceString("Description"));

		daysLeftLabel = new JLabel();
		daysLeftLabel.setText(Resource.getResourceString("Days_Left"));
		daysLeftLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		daysLeftLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		
		description = new javax.swing.JTextField();

		jPanel3 = new javax.swing.JPanel();
		itemtext = new javax.swing.JTextField();
		lblItemNum = new javax.swing.JLabel();
		lblStatus = new javax.swing.JLabel();
		startdatechooser = new JDateChooser();
		duedatechooser = new JDateChooser();
		lblStartDate = new javax.swing.JLabel();
		lblDueDate = new javax.swing.JLabel();

		statebox = new javax.swing.JComboBox();
		catlabel = new javax.swing.JLabel();
		
		jPanel4 = new javax.swing.JPanel();
		jButton2 = new javax.swing.JButton();

		jMenuBar1 = new javax.swing.JMenuBar();
		jMenu1 = new javax.swing.JMenu();
		jMenuItem1 = new javax.swing.JMenuItem();
		jMenuItem2 = new javax.swing.JMenuItem();

		setLayout(new java.awt.GridBagLayout());

		jPanel3.setLayout(new java.awt.GridBagLayout());

		jPanel3.setBorder(new javax.swing.border.TitledBorder(Resource
				.getResourceString("ProjectInformation")));

		itemtext.setText("itemtext");

		lblItemNum.setText(Resource.getResourceString("Item_#"));

		ResourceHelper.setText(lblStatus, "Status");
		lblStatus.setLabelFor(statebox);

		ResourceHelper.setText(lblStartDate, "Start_Date");
		lblStartDate.setLabelFor(startdatechooser);

		ResourceHelper.setText(lblDueDate, "Due_Date");
		lblDueDate.setLabelFor(duedatechooser);

		ResourceHelper.setText(catlabel, "Category");
		catlabel.setLabelFor(getCatbox());

		jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Save16.gif")));
		ResourceHelper.setText(jButton2, "Save");
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});

		jPanel4.add(jButton2, jButton2.getName());
		jPanel4.add(getGanttbutton());

		JButton projRptButton = new JButton();
		// projRptButton.setIcon(new
		// javax.swing.ImageIcon(getClass().getResource("/resource/Save16.gif")));
		ResourceHelper.setText(projRptButton, "Report");
		projRptButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				reportButtonActionPerformed(evt);
			}
		});
		jPanel4.add(projRptButton);

		jPanel3.add(lblStartDate, GridBagConstraintsFactory.create(3,1,GridBagConstraints.BOTH));
		jPanel3.add(lblDueDate, GridBagConstraintsFactory.create(1,4,GridBagConstraints.BOTH));
		jPanel3.add(catlabel, GridBagConstraintsFactory.create(3,0,GridBagConstraints.BOTH));
		jPanel3.add(duedatechooser, GridBagConstraintsFactory.create(2,4,GridBagConstraints.BOTH, 1.0, 0.0));
		jPanel3.add(startdatechooser, GridBagConstraintsFactory.create(4,1,GridBagConstraints.BOTH, 1.0, 0.0));
		jPanel3.add(lblItemNum, GridBagConstraintsFactory.create(1,0,GridBagConstraints.BOTH));
		jPanel3.add(itemtext, GridBagConstraintsFactory.create(2,0,GridBagConstraints.BOTH));
		jPanel3.add(lblStatus, GridBagConstraintsFactory.create(1,1,GridBagConstraints.BOTH));
		jPanel3.add(getCatbox(), GridBagConstraintsFactory.create(4,0,GridBagConstraints.BOTH, 1.0, 0.0));
		jPanel3.add(statebox, GridBagConstraintsFactory.create(2,1,GridBagConstraints.BOTH, 1.0, 0.0));
		jPanel3.add(getDaysLeftText(), GridBagConstraintsFactory.create(4,4,GridBagConstraints.BOTH, 1.0, 0.0));
		jPanel3.add(daysLeftLabel, GridBagConstraintsFactory.create(3,4));
		jPanel3.add(description, GridBagConstraintsFactory.create(2,5,GridBagConstraints.BOTH, 1.0, 0.0));
		jPanel3.add(descLabel, GridBagConstraintsFactory.create(1,5,GridBagConstraints.BOTH));
		jPanel3.add(totalLabel, GridBagConstraintsFactory.create(3,5));
		jPanel3.add(openLabel, GridBagConstraintsFactory.create(3,6));
		jPanel3.add(getTotalText(), GridBagConstraintsFactory.create(4,5,GridBagConstraints.BOTH, 1.0, 0.0));
		jPanel3.add(getOpenText(), GridBagConstraintsFactory.create(4,6,GridBagConstraints.BOTH, 1.0, 0.0));

		jPanel3.add(projBox, GridBagConstraintsFactory.create(2,6,GridBagConstraints.BOTH, 1.0, 0.0));

		if (!TaskModel.getReference().hasSubTasks())
			projBox.setEnabled(false);

		JLabel plab = new JLabel(Resource.getResourceString("parent"));
		jPanel3.add(plab, GridBagConstraintsFactory.create(1,6,GridBagConstraints.BOTH));

		GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
		
		gridBagConstraints25.gridx = 0;

		add(jPanel3, GridBagConstraintsFactory.create(0,0,GridBagConstraints.BOTH, 1.0, 0.0)); // Generated

		add(jPanel4, gridBagConstraints25);

		linkPanel.setBorder(new javax.swing.border.TitledBorder(Resource
				.getResourceString("links")));
		gridBagConstraints25.gridy = 2;
		gridBagConstraints25.fill = GridBagConstraints.BOTH;
		add(linkPanel, gridBagConstraints25);
		
		taskBorder = new JPanel();
		taskBorder.setBorder(new TitledBorder(Resource
				.getResourceString("tasks")));
		taskBorder.setLayout(new GridBagLayout());
		
		add(taskBorder, GridBagConstraintsFactory.create(0,3,GridBagConstraints.BOTH, 1.0, 1.0));
		

	}// GEN-END:initComponents

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jButton2ActionPerformed
	{// GEN-HEADEREND:event_jButton2ActionPerformed
		saveProject(evt);
	}// GEN-LAST:event_jButton2ActionPerformed

	private void reportButtonActionPerformed(java.awt.event.ActionEvent evt) {

		// get the task number from column 0 of the selected row
		String num = itemtext.getText();
		if (num.equals("NEW"))
			return;

		int pnum = Integer.parseInt(num);
		try {
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("pid", new Integer(pnum));
			Collection<Project> allChildren = TaskModel.getReference()
					.getAllSubProjects(pnum);
			Iterator<Project> it = allChildren.iterator();
			for (int i = 2; i <= 10; i++) {
				if (!it.hasNext())
					break;
				Project p = it.next();
				map.put("pid" + i, p.getKey());
			}
			RunReport.runReport("proj", map);
		} catch (NoClassDefFoundError r) {
			Errmsg.notice(Resource.getResourceString("borg_jasp"));
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	// save a task
	private void saveProject(java.awt.event.ActionEvent evt) {

		// save a task from the data on the screen
		if (description.getText() == null || description.getText().equals("")) {
			Errmsg.notice(Resource.getResourceString("empty_desc"));
			return;
		}
		try {

			String num = itemtext.getText();

			// allocate a new task object from the task data model
			TaskModel taskmod_ = TaskModel.getReference();

			Project p = new Project();

			// set the task number to the current number for updates and
			// -1 for new tasks. task model will convert -1 to next
			// available number

			if (num.equals("NEW")) {

				p.setKey(-1);

			} else if (num.equals("CLONE")) {
				p.setKey(-1);

			} else {
				p.setKey(Integer.parseInt(num));

			}

			// fill in the fields from the screen

			Calendar cal = startdatechooser.getCalendar();
			if (cal == null)
				cal = new GregorianCalendar();
			p.setStartDate(cal.getTime()); // start date
			cal = duedatechooser.getCalendar();

			if (cal != null) {
				p.setDueDate(cal.getTime()); // due date

				if (DateUtil.isAfter(p.getStartDate(), p.getDueDate())) {
					throw new Warning(Resource
							.getResourceString("sd_dd_warn"));
				}
			}
			p.setDescription(description.getText()); // description

			p.setStatus((String) statebox.getSelectedItem());
			String cat = (String) catbox.getSelectedItem();
			if (cat.equals("") || cat.equals(CategoryModel.UNCATEGORIZED)) {
				p.setCategory(null);
			} else {
				p.setCategory(cat);
			}

			p.setParent(null);
			String proj = (String) projBox.getSelectedItem();
			try {
				p.setParent(getProjectId(proj));

			} catch (Exception e) {
				// no project selected
			}

			taskmod_.saveProject(p);
			p.setKey(p.getKey());

			showProject(T_CHANGE, p, null);
		} catch (Warning w) {
			Errmsg.notice(w.getMessage());
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}// GEN-LAST:event_savetask

	private void showProject(int function, Project p, Integer parentId)
			throws Exception {

		linkPanel.setOwner(p);

		projBox.removeAllItems();
		projBox.addItem("");
		Collection<Project> projects = TaskModel.getReference().getProjects();
		if (projects != null) {
			Iterator<Project> pi = projects.iterator();
			while (pi.hasNext()) {
				Project p2 = pi.next();
				if ((p == null || p.getKey() != p2.getKey())
						&& p2.getStatus().equals(
								Resource.getResourceString("OPEN")))
					projBox.addItem(getProjectString(p2));
			}
		}

		// if we are showing an existing task - fil; in the gui fields form it
		if (p != null) {
			// task number
			itemtext.setText(Integer.toString(p.getKey()));
			itemtext.setEditable(false);

			// window title - "Item N"
			title_ = Resource.getResourceString("Item_") + p.getKey();

			// due date
			GregorianCalendar gc = new GregorianCalendar();
			Date dd = p.getDueDate();
			if (dd != null) {
				gc.setTime(dd);
				duedatechooser.setCalendar(gc);
			}

			GregorianCalendar gc2 = new GregorianCalendar();
			dd = p.getStartDate();
			if (dd != null)
				gc2.setTime(dd);

			startdatechooser.setCalendar(gc2);

			int daysleft = TaskModel.daysLeft(p.getDueDate());
			daysLeftText.setText(Integer.toString(daysleft));

			// cattext.setText( task.getCategory() );
			String cat = p.getCategory();
			if (cat != null && !cat.equals("")) {
				catbox.setSelectedItem(cat);
			} else {
				catbox.setSelectedIndex(0);
			}

			description.setText(p.getDescription()); // description

			// statebox.addItem(task.getState()); // state
			statebox.setEditable(false);

			Collection<Task> ptasks = TaskModel.getReference().getTasks(
					p.getKey());
			totalText.setText(Integer.toString(ptasks.size()));
			int open = 0;
			Iterator<Task> it = ptasks.iterator();
			while (it.hasNext()) {
				Task pt = it.next();
				if (!TaskModel.isClosed(pt)) {
					open++;
				}
			}
			openText.setText(Integer.toString(open));

			Integer pid = p.getParent();
			if (pid != null) {
				Project par = TaskModel.getReference().getProject(
						pid.intValue());
				if (TaskModel.isClosed(par)) {
					projBox.addItem(getProjectString(par));
				}
				projBox.setSelectedItem(getProjectString(par));

			}

			// add the task list
			if (taskPanel == null) {
				taskPanel = new TaskListPanel(TaskView.getProjectString(p),
						Resource.getResourceString("All"), "", false);
				GridBagConstraints taskGBC = new GridBagConstraints();
				taskGBC.gridx = 0;
				taskGBC.gridy = 0;
				taskGBC.weightx = 1.0;
				taskGBC.weighty = 1.0;
				taskGBC.fill = GridBagConstraints.BOTH;
				taskBorder.add(taskPanel, taskGBC);
				
			}

		} else // initialize new task
		{

			// task number = NEW
			itemtext.setText("NEW");
			itemtext.setEditable(false);

			// title
			title_ = Resource.getResourceString("NEW_Item");
			statebox.addItem(Resource.getResourceString("OPEN"));
			statebox.setEnabled(false);
			catbox.setSelectedIndex(0);
			description.setText(""); // desc
			totalText.setText("");
			openText.setText("");
			// duedatechooser.setCalendar(new GregorianCalendar());
			// startdatechooser.setCalendar(new GregorianCalendar());

			if (parentId != null) {
				Project par = TaskModel.getReference().getProject(
						parentId.intValue());
				if (TaskModel.isClosed(par)) {
					projBox.addItem(getProjectString(par));
				}
				projBox.setSelectedItem(getProjectString(par));

				String cat = par.getCategory();
				if (cat != null && !cat.equals("")) {
					catbox.setSelectedItem(cat);
				} else {
					catbox.setSelectedIndex(0);
				}

				GregorianCalendar gc = new GregorianCalendar();
				Date dd = par.getDueDate();
				if (dd != null) {
					gc.setTime(dd);
					duedatechooser.setCalendar(gc);
				}

				Date sd = par.getStartDate();
				if (sd != null) {
					gc.setTime(sd);
					startdatechooser.setCalendar(gc);
				}

			}

		}

		if (p == null) {
			// statebox.addItem(taskmod_.getTaskTypes().getInitialState(
			// typebox.getSelectedItem().toString()));
			statebox.setEnabled(false);
		}

		// cloning takes the fields filled in for an existing task and resets
		// only those
		// that don't apply to the clone
		if (function == T_CLONE) {
			// need new task number
			itemtext.setText("CLONE");
			itemtext.setEditable(false);

			statebox.removeAllItems();
			statebox.addItem(Resource.getResourceString("OPEN"));
			statebox.setEnabled(false);

		}
		// change existing task
		else if (function == T_CHANGE) {

			// determine valid next states based on task type and current
			// state
			String state = null;
			if (p != null)
				state = p.getStatus();

			// set next state pulldown
			statebox.removeAllItems();
			statebox.addItem(Resource.getResourceString("OPEN"));
			statebox.addItem(Resource.getResourceString("CLOSED"));
			statebox.setSelectedItem(state);
			statebox.setEnabled(true);

		}

	}
} // @jve:decl-index=0:visual-constraint="115,46"

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

package net.sf.borg.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.Task;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.InputDialog;

/**
 * UI for choosing which categories to display and which to hide
 */
public class CategoryChooser extends View {

	private static final long serialVersionUID = 1L;

	/** The singleton. */
	private static CategoryChooser singleton = null;

	/**
	 * Gets the singleton.
	 * 
	 * @return the singleton
	 */
	public static CategoryChooser getReference() {
		if (singleton == null || !singleton.isShowing())
			singleton = new CategoryChooser();
		return (singleton);
	}

	/** The apply button */
	private javax.swing.JButton applyButton;

	private javax.swing.JPanel buttonPanel;

	/** category check boxes */
	private ArrayList<JCheckBox> cbs = null;

	private javax.swing.JPanel checkBoxPanel;

	/** The clear button. */
	private JButton clearAllButton = null;

	private JMenuItem delcatMI;
	/** The dismiss button. */
	private javax.swing.JButton dismissButton;
	private JScrollPane jScrollPane = null;
	/** The select all button */
	private JButton selectAllButton = null;

	private JPanel topPanel = null;

	/**
	 *constructor
	 */
	private CategoryChooser() {

		initComponents();

		try {
			CategoryModel catmod = CategoryModel.getReference();
			Collection<String> curcats = catmod.getShownCategories();
			Collection<String> allcats = catmod.getCategories();

			if (allcats == null) {
				allcats = new TreeSet<String>();
			}

			// add check boxes for every category
			cbs = new ArrayList<JCheckBox>();

			Iterator<String> it = allcats.iterator();
			while (it.hasNext()) {
				String cat = it.next();
				JCheckBox cb = new JCheckBox(cat);
				cbs.add(cb);

				// set all currently shown categories to be selected
				if (curcats != null && curcats.contains(cat))
					cb.setSelected(true);

				checkBoxPanel.add(cb);
			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		pack();
	}

	/**
	 * apply button action performed.
	 * 
	 * @param evt
	 *            the evt
	 */
	private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// set categories
		TreeSet<String> newcats = new TreeSet<String>();
		Iterator<JCheckBox> it = cbs.iterator();
		while (it.hasNext()) {
			JCheckBox cb = it.next();
			if (cb.isSelected()) {
				newcats.add(cb.getText());
			}
		}

		// set the shown categories in the model
		CategoryModel.getReference().setShownCategories(newcats);

	}

	@Override
	public void destroy() {
		this.dispose();
	}

	/**
	 * dismiss button action performed.
	 * 
	 * @param evt
	 *            the evt
	 */
	private void dismissButtonActionPerformed(java.awt.event.ActionEvent evt) {
		this.dispose();
	}

	/**
	 * Exit the window.
	 * 
	 * @param evt
	 *            the evt
	 */
	private void exitForm(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_exitForm
		this.dispose();
	}

	/**
	 * builds and returns the Category menu items
	 * 
	 * @return the Category JMenu
	 */
	public JMenu getCategoryMenu() {
		/*
		 * 
		 * Categories Menu
		 */
		JMenu catmenu = new JMenu();

		catmenu.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Preferences16.gif")));
		ResourceHelper.setText(catmenu, "Categories");

		JMenuItem chooseCategoriesMI = new JMenuItem();
		chooseCategoriesMI.setIcon(new javax.swing.ImageIcon(getClass()
				.getResource("/resource/Preferences16.gif")));
		ResourceHelper.setText(chooseCategoriesMI, "choosecat");
		chooseCategoriesMI.setActionCommand("Choose Displayed Categories");
		chooseCategoriesMI
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						CategoryChooser.getReference().setVisible(true);
					}
				});
		catmenu.add(chooseCategoriesMI);

		JMenuItem addCategoryMI = new JMenuItem();
		addCategoryMI.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Add16.gif")));
		ResourceHelper.setText(addCategoryMI, "addcat");
		addCategoryMI.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				String inputValue = InputDialog.show(Resource
						.getResourceString("AddCat"), 15);
				if (inputValue == null || inputValue.equals(""))
					return;
				try {
					CategoryModel.getReference().addCategory(inputValue);
					CategoryModel.getReference().showCategory(inputValue);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});
		catmenu.add(addCategoryMI);

		JMenuItem removeCategoryMI = new JMenuItem();
		removeCategoryMI.setIcon(new javax.swing.ImageIcon(getClass()
				.getResource("/resource/Delete16.gif")));
		ResourceHelper.setText(removeCategoryMI, "remcat");
		removeCategoryMI.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					CategoryModel.getReference().syncCategories();
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});
		catmenu.add(removeCategoryMI);

		catmenu.add(getDelcatMI());
		
		return catmenu;

	}

	/**
	 * This method initializes clearAllButton.
	 * 
	 * @return clearAllButton
	 */
	private JButton getClearButton() {
		if (clearAllButton == null) {
			clearAllButton = new JButton();
			ResourceHelper.setText(clearAllButton, "clear_all");
			clearAllButton
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(java.awt.event.ActionEvent e) {
							Iterator<JCheckBox> it = cbs.iterator();
							while (it.hasNext()) {
								// clear all selected check boxes
								JCheckBox cb = it.next();
								cb.setSelected(false);
							}
						}
					});
		}
		return clearAllButton;
	}

	/**
	 * delete category menu item
	 */
	private JMenuItem getDelcatMI() {
		if (delcatMI == null) {
			delcatMI = new JMenuItem();
			ResourceHelper.setText(delcatMI, "delete_cat");
			delcatMI.setIcon(new javax.swing.ImageIcon(getClass().getResource(
					"/resource/Delete16.gif")));
			delcatMI.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {

					try {

						// get category list
						CategoryModel catmod = CategoryModel.getReference();
						Collection<String> allcats = catmod.getCategories();
						allcats.remove(CategoryModel.UNCATEGORIZED);
						if (allcats.isEmpty())
							return;
						Object[] cats = allcats.toArray();

						// ask user to choose a category
						Object o = JOptionPane.showInputDialog(null, Resource
								.getResourceString("delete_cat_choose"), "",
								JOptionPane.QUESTION_MESSAGE, null, cats,
								cats[0]);
						if (o == null)
							return;

						// confirm with user
						int ret = JOptionPane.showConfirmDialog(null, Resource
								.getResourceString("delcat_warn")
								+ " [" + (String) o + "]!", "",
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.WARNING_MESSAGE);
						if (ret == JOptionPane.OK_OPTION) {

							// deletes all appts and tasks with that cetegory
							// !!!!!

							// appts
							Iterator<?> itr = AppointmentModel.getReference()
									.getAllAppts().iterator();
							while (itr.hasNext()) {
								Appointment ap = (Appointment) itr.next();
								String cat = ap.getCategory();
								if (cat != null && cat.equals(o))
									AppointmentModel.getReference().delAppt(ap);
							}

							// tasks
							itr = TaskModel.getReference().getTasks()
									.iterator();
							while (itr.hasNext()) {
								Task t = (Task) itr.next();
								String cat = t.getCategory();
								if (cat != null && cat.equals(o))
									TaskModel.getReference().delete(t.getKey());
							}

							try {
								CategoryModel.getReference().syncCategories();
							} catch (Exception ex) {
								Errmsg.errmsg(ex);
							}
						}
					} catch (Exception ex) {
						Errmsg.errmsg(ex);
					}
				}
			});
		}
		return delcatMI;
	}

	/**
	 * This method initializes jScrollPane.
	 * 
	 * @return jScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setPreferredSize(new java.awt.Dimension(40, 200));
			jScrollPane.setViewportView(checkBoxPanel);
		}
		return jScrollPane;
	}

	/**
	 * This method initializes topPanel.
	 * 
	 * @return topPanel
	 */
	private JPanel getTopPanel() {
		if (topPanel == null) {
			topPanel = new JPanel();
			topPanel.setLayout(new GridBagLayout());
			topPanel.add(buttonPanel, GridBagConstraintsFactory.create(0, 1,
					GridBagConstraints.BOTH, 1.0, 0.0));
			topPanel.add(getJScrollPane(), GridBagConstraintsFactory.create(0,
					0, GridBagConstraints.BOTH, 1.0, 1.0));
		}
		return topPanel;
	}

	/**
	 * initialize the UI components
	 */
	private void initComponents() {

		GridLayout gridLayout2 = new GridLayout();
		checkBoxPanel = new javax.swing.JPanel();
		buttonPanel = new javax.swing.JPanel();
		applyButton = new javax.swing.JButton();
		dismissButton = new javax.swing.JButton();

		getContentPane().setLayout(new java.awt.GridBagLayout());

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		this.setTitle(Resource.getResourceString("catchooser"));
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});

		checkBoxPanel.setLayout(new java.awt.GridLayout(0, 1));

		checkBoxPanel.setBorder(new javax.swing.border.LineBorder(
				new java.awt.Color(0, 0, 0), 1, true));
		buttonPanel.setLayout(gridLayout2);

		getContentPane().add(
				checkBoxPanel,
				GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH,
						1.0, 1.0));

		applyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Save16.gif")));
		ResourceHelper.setText(applyButton, "apply");
		applyButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				applyButtonActionPerformed(evt);
			}
		});

		dismissButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Stop16.gif")));
		ResourceHelper.setText(dismissButton, "Dismiss");
		dismissButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				dismissButtonActionPerformed(evt);
			}
		});
		setDismissButton(dismissButton);

		this.setSize(250, 147);
		this.setContentPane(getTopPanel());
		gridLayout2.setRows(1);
		buttonPanel.add(selectAllButton(), null);
		buttonPanel.add(getClearButton(), null);
		buttonPanel.add(applyButton, null);
		buttonPanel.add(dismissButton, null);
		getContentPane().add(
				buttonPanel,
				GridBagConstraintsFactory.create(0, 1,
						GridBagConstraints.HORIZONTAL));

		pack();
	}
	@Override
	public void refresh() {
	  // empty
	}
	
	@Override
	public void update(ChangeEvent event) {
		refresh();
	}
	
	/**
	 * This method initializes selectAllButton.
	 * 
	 * @return selectAllButton
	 */
	private JButton selectAllButton() {
		if (selectAllButton == null) {
			selectAllButton = new JButton();
			ResourceHelper.setText(selectAllButton, "select_all");
			selectAllButton
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(java.awt.event.ActionEvent e) {
							Iterator<JCheckBox> it = cbs.iterator();
							while (it.hasNext()) {
								// select all check boxes
								JCheckBox cb = it.next();
								cb.setSelected(true);
							}
						}
					});
		}
		return selectAllButton;
	}
}

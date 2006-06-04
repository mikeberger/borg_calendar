package net.sf.borg.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.ResultSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import net.sf.borg.common.ui.ScrolledDialog;
import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.Resource;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.db.jdbc.JdbcDB;

public class SqlRunner extends JDialog {
	

	public SqlRunner() {

		super();
		
		setModal(true);
		
		// init the gui components
		initComponents();

		
		this.setTitle(Resource.getPlainResourceString("RunSQL"));

		pack();

	}





	private void initComponents()// GEN-BEGIN:initComponents
	{

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0; // Generated
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH; // Generated
		gridBagConstraints.gridy = 1; // Generated
		this.getContentPane().setLayout(new GridBagLayout()); // Generated

		editor = new javax.swing.JEditorPane();
		jPanel1 = new javax.swing.JPanel();
		runButton = new javax.swing.JButton();

		editor.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(
				0, 0, 0)));

		jScrollPane1 = new JScrollPane();
		jScrollPane1.setPreferredSize(new java.awt.Dimension(554, 404));
		jScrollPane1.setViewportView(editor);

		runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Forward16.gif")));
		
		runButton.setText("Run");
		runButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				runbuttonActionPerformed(evt);
			}
		});

		jPanel1.add(runButton);
		

		clearButton = new JButton();
		clearButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Undo16.gif")));
		clearButton.setText("Clear");
		clearButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				clearbuttonActionPerformed(evt);
			}
		});

		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.weighty = 1.0;
		gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints1.insets = new java.awt.Insets(4, 4, 4, 4);
		this.getContentPane().add(jScrollPane1, gridBagConstraints1);

		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.gridy = 2;
		gridBagConstraints2.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;

		jPanel1.add(clearButton, clearButton.getName());
		this.getContentPane().add(jPanel1, gridBagConstraints2); // Generated
		

	}


	private void runbuttonActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			
			// System.out.println(sb.toString());
			JdbcDB.startTransaction();
			ResultSet r = JdbcDB.execSQL(editor.getText());
			String res = "";
			if( r != null && r.next())
			{
				int cols = r.getMetaData().getColumnCount();
				for( ;!r.isAfterLast();r.next())
				{
					for( int i = 1; i <= cols; i++)
					{
						res += r.getString(i) + " ";
					}
					res += "\n";
				}
			}
			JdbcDB.commitTransaction();
			if( !res.equals(""))
				ScrolledDialog.showNotice(res);
				//JOptionPane.showMessageDialog(this, res);
			else
				ScrolledDialog.showNotice(Resource.getPlainResourceString("noOutput"));
				//JOptionPane.showMessageDialog(this, Resource.getPlainResourceString("noOutput"));
		} catch (Exception e) {
			System.out.println(e.toString());
			try {
				JdbcDB.rollbackTransaction();
			} catch (Exception e2) {
			}
			Errmsg.errmsg(e);
		}
		
		AppointmentModel.getReference().refresh();
		TaskModel.getReference().refresh();
		AddressModel.getReference().refresh();
		
		
	}

	private void clearbuttonActionPerformed(java.awt.event.ActionEvent evt) {
		editor.setText("");
	}

	private javax.swing.JPanel jPanel1;

	private javax.swing.JScrollPane jScrollPane1;

	private JEditorPane editor;

	private javax.swing.JButton runButton;

	private JButton clearButton;

	public static void main( String args[])
	{
		new SqlRunner().setVisible(true);
	}
}

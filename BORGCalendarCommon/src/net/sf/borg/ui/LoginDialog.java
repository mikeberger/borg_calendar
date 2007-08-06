package net.sf.borg.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import net.sf.borg.common.Errmsg;

public class LoginDialog extends JDialog
{
public LoginDialog(Frame frmParent)
{
	super
	(
		frmParent,
		"Log in",
		true
	);
	initUI();
}

// Accessors
public final String getUsername()
{
	return m_txfUsername.getText();
}

public final String getPassword()
{
	return m_txfPassword.getText();
}

// private //
private JTextField m_txfUsername, m_txfPassword;

private void initUI()
{
	initCtrls();
	pack();
	setLocationRelativeTo(null);
}

private void initCtrls()
{
	JPanel pnlMain = new JPanel();
	getContentPane().add(pnlMain);
	pnlMain.setLayout(new BorderLayout());
	
	JPanel pnlInputAndIcon = new JPanel();
	pnlMain.add(pnlInputAndIcon, BorderLayout.CENTER);
	pnlInputAndIcon.setLayout(new BorderLayout());
	
	JPanel pnlInput = new JPanel();
	pnlInputAndIcon.add(pnlInput, BorderLayout.CENTER);
	pnlInput.setLayout(new BorderLayout());
	
	JPanel pnlIcon = new JPanel();
	pnlInputAndIcon.add(pnlIcon, BorderLayout.WEST);
	pnlIcon.setLayout(new BorderLayout());
//	pnlIcon.add(new JLabel(new ImageIcon(getClass().getResource("/resource/borg.jpg"))));
	
	JPanel pnlFields = new JPanel();
	pnlInput.add(pnlFields, BorderLayout.CENTER);
	pnlFields.setLayout(new GridLayout(0,1));
	pnlFields.add(m_txfUsername = new JTextField(20));
	pnlFields.add(m_txfPassword = new JPasswordField(20));
	
	JPanel pnlLabels = new JPanel();
	pnlInput.add(pnlLabels, BorderLayout.WEST);
	pnlLabels.setLayout(new GridLayout(0,1));
	
	JLabel lblUserId, lblPassword;
	pnlLabels.add(lblUserId = new JLabel("User ID:"));
	lblUserId.setDisplayedMnemonic(KeyEvent.VK_U);
	lblUserId.setLabelFor(m_txfUsername);
	pnlLabels.add(lblPassword = new JLabel("Password:"));
	lblPassword.setDisplayedMnemonic(KeyEvent.VK_P);
	lblPassword.setLabelFor(m_txfPassword);
	
	JPanel pnlButtons = new JPanel();
	pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
	pnlMain.add(pnlButtons, BorderLayout.SOUTH);
	JButton bn;
	pnlButtons.add(bn = new JButton("OK"));
	getRootPane().setDefaultButton(bn);
	bn.addActionListener
	(
		new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (!checkNonEmpty(m_txfUsername, "Username")) return;
				if (!checkNonEmpty(m_txfPassword, "Password")) return;
				setVisible(false);
			}
		}
	);

	pnlButtons.add(bn = new JButton("Cancel"));
	ActionListener cancelListener =
		new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				m_txfUsername.setText("");
				m_txfPassword.setText("");
				setVisible(false);
			}
		};
	bn.addActionListener(cancelListener);
	getRootPane()
		.registerKeyboardAction
		(
			cancelListener,
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW
		);
}
	
// private //
private static boolean checkNonEmpty(JTextField txf, String name)
{
	if (txf.getText().length() == 0)
	{
		Errmsg.notice(name+" must not be empty.");
		txf.requestFocus();
		return false;
	}
	return true;
}
}

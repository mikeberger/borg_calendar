/* This code was found posted on a forum without any copyright
 * restrictions. It was written by someone named Noah W.
 * That's all I know */

/* I have modified the original code but claim no copyright on it of any kind */

package net.sf.borg.ui.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.FontUIResource;

import net.sf.borg.common.Resource;

//
// FontChooser by Noah w.
//
/**
 * font chooser dialog
 */
// I am not going to clean this up or comment. It is public domain code.
public class NwFontChooserS extends JDialog {
    private static final long serialVersionUID = 1L;
    String[] styleList = new String[] { "Plain", "Bold", "Italic" };
    String[] sizeList =
    new String[] {
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10",
        "11",
        "12",
        "13",
        "14",
        "15",
        "16",
        "17",
        "18",
        "19",
        "20",
        "22",
        "24",
        "27",
        "30",
        "34",
        "39",
        "45",
        "51",
        "60" };
        NwList StyleList;
        NwList FontList;
        NwList SizeList;
        static JLabel Sample = new JLabel();
        boolean ob = false;
        
        private NwFontChooserS(Frame parent, boolean modal, Font fontIn) {
            super(parent, modal);
            Font font = fontIn;
            initAll();
            setTitle("Font Choosr");
            if (font == null)
                font = Sample.getFont();
            FontList.setSelectedItem(font.getName());
            SizeList.setSelectedItem(font.getSize() + "");
            StyleList.setSelectedItem(styleList[font.getStyle()]);
            
        }
        public static Font showDialog(Frame parent, String s, Font font) {
            NwFontChooserS fd = new NwFontChooserS(parent, true, font);
            if (s != null)
                fd.setTitle(s);
            fd.setVisible(true);
            Font fo = null;
            if (fd.ob)
                fo = Sample.getFont();
            fd.dispose();
            return (fo);
        }
        private void initAll() {
            getContentPane().setLayout(null);
            setBounds(50, 50, 450, 450);
            addLists();
            addButtons();
            Sample.setBounds(10, 320, 415, 25);
            Sample.setForeground(Color.black);
            getContentPane().add(Sample);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setVisible(false);
                }
            });
        }
        private void addLists() {
            FontList =
            new NwList(
            GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames());
            StyleList = new NwList(styleList);
            SizeList = new NwList(sizeList);
            FontList.setBounds(10, 10, 260, 295);
            StyleList.setBounds(280, 10, 80, 295);
            SizeList.setBounds(370, 10, 40, 295);
            getContentPane().add(FontList);
            getContentPane().add(StyleList);
            getContentPane().add(SizeList);
        }
        private void addButtons() {
            JButton ok = new JButton(Resource.getResourceString("OK"));
            ok.setMargin(new Insets(0, 0, 0, 0));
            JButton ca = new JButton(Resource.getResourceString("Cancel"));
            ca.setMargin(new Insets(0, 0, 0, 0));
            ok.setBounds(260, 350, 70, 20);
            ok.setFont(new Font(" ", 1, 11));
            ca.setBounds(340, 350, 70, 20);
            ca.setFont(new Font(" ", 1, 12));
            getContentPane().add(ok);
            getContentPane().add(ca);
            ok.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    ob = true;
                }
            });
            ca.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    ob = false;
                }
            });
        }
        private void showSample() {
            int g = 0;
            try {
                g = Integer.parseInt(SizeList.getSelectedValue());
            }
            catch (NumberFormatException nfe) {
              // empty
            }
            String st = StyleList.getSelectedValue();
            int s = Font.PLAIN;
            if (st.equalsIgnoreCase("Bold"))
                s = Font.BOLD;
            if (st.equalsIgnoreCase("Italic"))
                s = Font.ITALIC;
            Sample.setFont(new Font(FontList.getSelectedValue(), s, g));
            Sample.setText("The quick brown fox jumped over the lazy dog.");
        }
        //////////////////////////////////////////////////////////////////////
        private class NwList extends JPanel {
            private static final long serialVersionUID = 1L;
            JList jl;
            JScrollPane sp;
            JLabel jt;
            String si = " ";
            
            public NwList(String[] values) {
                setLayout(null);
                jl = new JList(values);
                sp = new JScrollPane(jl);
                jt = new JLabel();
                jt.setBackground(Color.white);
                jt.setForeground(Color.black);
                jt.setOpaque(true);
                jt.setBorder(new JTextField().getBorder());
                jt.setFont(getFont());
                jl.setBounds(0, 0, 100, 1000);
                jl.setBackground(Color.white);
                jl.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        jt.setText((String) jl.getSelectedValue());
                        si = (String) jl.getSelectedValue();
                        showSample();
                    }
                });
                add(sp);
                add(jt);
            }
            public String getSelectedValue() {
                return (si);
            }
            public void setSelectedItem(String s) {
                jl.setSelectedValue(s, true);
            }
            @Override
            public void setBounds(int x, int y, int w, int h) {
                super.setBounds(x, y, w, h);
                sp.setBounds(0, y + 12, w, h - 23);
                sp.revalidate();
                jt.setBounds(0, 0, w, 20);
            }
        }
        
        static public String fontString(Font font) {
            String fs = font.getFamily();
            if( !font.isPlain() ) {
                fs += "-";
                if( font.isBold()) {
                    fs += "BOLD";
                }
                if( font.isItalic()) {
                    fs += "ITALIC";
                }
            }
            fs += "-" + font.getSize();
            return(fs);
        }
        
        static public void setDefaultFont(Font f ) {
            FontUIResource fui = new FontUIResource(f);
            Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof FontUIResource)
                    UIManager.put(key, fui);
            }
            
        }
        
        public static void main(String args[]) {
            Font font = null;
            font = NwFontChooserS.showDialog(null, null, null);
            
            System.out.println(fontString(font));
        }
}

package net.sf.borg.ui.util;

import lombok.Getter;
import lombok.Setter;
import net.sf.borg.common.Resource;
import net.sf.borg.model.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ColorComboBox extends JComboBox<String>{

    /**
     * renders the color selection pull-down with colored boxes as the choices.
     */
    static private class ColorBoxRenderer extends JLabel implements ListCellRenderer<Object> {

        private static final long serialVersionUID = 1L;

        @Getter
        @Setter
        private Color chosenColor = Color.black;

        /**
         * constructor.
         */
        public ColorBoxRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
        }

        /**
         * get the color choice label for a given color value.
         */
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            String sel = (String) value;

            if (isSelected) {
                setBackground(list.getSelectionBackground());
            } else {
                setBackground(list.getBackground());
            }

            // no text - the label will be icon only
            setText(" ");

            // set the icon based on the user defined colors
            // map the "logical" color names to the user defined color
            Theme t = Theme.getCurrentTheme();
            if (sel.equals(colors[3])) {
                setIcon(new SolidComboBoxIcon(new Color(t.getTextColor4())));
            } else if (sel.equals(colors[0])) {
                setIcon(new SolidComboBoxIcon(new Color(t.getTextColor1())));
            } else if (sel.equals(colors[1])) {
                setIcon(new SolidComboBoxIcon(new Color(t.getTextColor2())));
            } else if (sel.equals(colors[2])) {
                setIcon(new SolidComboBoxIcon(new Color(t.getTextColor3())));
            } else if (sel.equals(colors[4])) {
                setIcon(new SolidComboBoxIcon(new Color(t.getTextColor5())));
            } else if (sel.equals("chosen")) {
                setIcon(new SolidComboBoxIcon(chosenColor));
            } else if (sel.equals("choose")) {
                setText(Resource.getResourceString("choose"));
                setIcon(null);
            } else {
                // just for strike-through, we use text
                setText(Resource.getResourceString("strike"));
                setIcon(null);
            }

            return this;
        }

    }

    static private class ComboItemListener implements ItemListener {

        @Getter
        @Setter
        private boolean active = true;

        private ColorBoxRenderer cbr;
        private JComboBox<String> box;

        public ComboItemListener(ColorBoxRenderer cbr, JComboBox<String> colorComboBox) {
            this.cbr = cbr;
            box = colorComboBox;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (!active)
                return;
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String l = (String) e.getItem();
                if (l.equals("choose")) {
                    Color selected = JColorChooser.showDialog(null, "", cbr.getChosenColor());
                    if (selected != null)
                        cbr.setChosenColor(selected);
                    box.setSelectedIndex(6);
                }

            }
        }

    };

    /**
     * Long, thin, rectangular icon that goes in the color chooser pulldown list
     * items.
     */
    static private class SolidComboBoxIcon implements Icon {

        private Color color = Color.BLACK; // color
        static private final int h = 10; // height
        static private final int w = 60; // width

        /**
         * Instantiates a new solid combo box icon.
         *
         * @param col
         *            the color
         */
        public SolidComboBoxIcon(Color col) {
            color = col;
        }

        /*
         * (non-Javadoc)
         *
         * @see Icon#getIconHeight()
         */
        @Override
        public int getIconHeight() {
            return h;
        }

        /*
         * (non-Javadoc)
         *
         * @see Icon#getIconWidth()
         */
        @Override
        public int getIconWidth() {
            return w;
        }

        /*
         * (non-Javadoc)
         *
         * @see Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
         */
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, w, h);
            g2.setColor(color);
            g2.fillRect(x, y, w, h);
        }
    }

    private final ColorBoxRenderer cbr = new ColorBoxRenderer();

    private final ComboItemListener comboItemListener = new ComboItemListener(cbr, this);

    private static final String colors[] = { Theme.COLOR1, Theme.COLOR2, Theme.COLOR3, Theme.COLOR4, Theme.COLOR5, "strike", "chosen", "choose" };

    public ColorComboBox() {

        this.setOpaque(false);
        this.setRenderer(cbr);
        this.setEditable(false);
        for (int i = 0; i < colors.length; i++) {
            this.addItem(colors[i]);
        }

        this.addItemListener(comboItemListener);
    }

    public Color getChosenColor(){
        return cbr.getChosenColor();
    }

    public void setChosenColor(Color color){
        cbr.setChosenColor(color);
    }

    public void setActive(boolean active){
        comboItemListener.setActive(active);
    }
}

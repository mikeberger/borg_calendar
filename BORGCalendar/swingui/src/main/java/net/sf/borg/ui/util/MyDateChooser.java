package net.sf.borg.ui.util;

import com.toedter.calendar.IDateEditor;
import com.toedter.calendar.JDateChooser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.util.Calendar;
import java.util.Date;

public class MyDateChooser extends JDateChooser {

    @Serial
    private static final long serialVersionUID = 120268996535847635L;

    public MyDateChooser(IDateEditor var1) {
        super(var1);
        this.calendarButton.addActionListener(this);
        calendarButton.setMnemonic(KeyEvent.VK_F24); // set to harmless key

    }

    public MyDateChooser()
    {
        super(new PlainDateEditor());
        this.calendarButton.addActionListener(this);
        calendarButton.setMnemonic(KeyEvent.VK_F24); // set to harmless key
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        int x = this.calendarButton.getWidth() - (int)this.popup.getPreferredSize().getWidth();
        int y = this.calendarButton.getY() + this.calendarButton.getHeight();
        Calendar var4 = Calendar.getInstance();
        Date var5 = this.dateEditor.getDate();
        if (var5 != null) {
            var4.setTime(var5);
        }

        this.jcalendar.setCalendar(var4);

        // adjust y if no room due to windows taskbar. Java adjusts based
        // on screen bottom, but ignores the taskbar

        int popUpHeight = popup.getPreferredSize().height;
        //determines the max available size of the screen
        Rectangle windowBounds = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getMaximumWindowBounds();
        Point invokerPosition = this.calendarButton.getLocationOnScreen();

        if (invokerPosition.y + popUpHeight > windowBounds.height) {
            //get the negative margin and use it as the y-offset
            y = windowBounds.height - (invokerPosition.y + popUpHeight);
        }

        this.popup.show(this.calendarButton, x, y);
        this.dateSelected = false;
    }
}

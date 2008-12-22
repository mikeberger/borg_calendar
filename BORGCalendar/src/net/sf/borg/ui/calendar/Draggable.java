package net.sf.borg.ui.calendar;

import java.util.Date;

interface Draggable extends Box {

    public abstract void move(int realtime, Date d) throws Exception;
}

package net.sf.borg.model;

import java.util.Date;
import java.util.List;

import net.sf.borg.model.entity.CalendarEntity;

public interface CalendarEntityProvider {
	
	List<CalendarEntity> getEntities(Date d);

}

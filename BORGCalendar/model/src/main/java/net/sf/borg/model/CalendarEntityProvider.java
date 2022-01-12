package net.sf.borg.model;

import net.sf.borg.model.entity.CalendarEntity;

import java.util.Date;
import java.util.List;

public interface CalendarEntityProvider {
	
	List<CalendarEntity> getEntities(Date d);

}

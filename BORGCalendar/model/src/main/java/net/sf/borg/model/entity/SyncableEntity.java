package net.sf.borg.model.entity;

import java.util.Date;

public interface SyncableEntity {
	
	enum ObjectType {
		APPOINTMENT, TASK, PROJECT, SUBTASK, REMOTE
	}
	
	int getKey();
	
	Date getCreateTime();

	Date getLastMod();

	String getUid();

	String getUrl();
	
	ObjectType getObjectType();

//	public void setCreateTime(Date d);
//
//	public void setLastMod(Date d);
//
//	public void setUid(String s);
//
//	public void setUrl(String s);
}

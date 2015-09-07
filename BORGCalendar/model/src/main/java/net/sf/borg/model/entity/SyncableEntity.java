package net.sf.borg.model.entity;

import java.util.Date;

public interface SyncableEntity {
	
	public static enum ObjectType {
		APPOINTMENT, TASK, PROJECT, SUBTASK, REMOTE
	}
	
	public int getKey();
	
	public Date getCreateTime();

	public Date getLastMod();

	public String getUid();

	public String getUrl();
	
	public ObjectType getObjectType();

//	public void setCreateTime(Date d);
//
//	public void setLastMod(Date d);
//
//	public void setUid(String s);
//
//	public void setUrl(String s);
}

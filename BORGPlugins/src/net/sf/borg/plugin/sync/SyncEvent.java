package net.sf.borg.plugin.sync;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sf.borg.model.Model.ChangeEvent.ChangeAction;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "SyncEvent")
@XmlAccessorType(XmlAccessType.FIELD)
public class SyncEvent {

	public static enum ObjectType {
		APPOINTMENT, TASK, PROJECT, SUBTASK
	}

	private Integer id;
	private ChangeAction action;
	private ObjectType objectType;

}

package net.sf.borg.model.ical;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sf.borg.model.Model.ChangeEvent.ChangeAction;
import net.sf.borg.model.entity.SyncableEntity.ObjectType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "SyncEvent")
@XmlAccessorType(XmlAccessType.FIELD)
public class SyncEvent {

	

	private Integer id;
	private String uid;
	private String url;
	private ChangeAction action;
	private ObjectType objectType;

}

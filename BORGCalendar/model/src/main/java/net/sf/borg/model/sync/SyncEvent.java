package net.sf.borg.model.sync;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sf.borg.model.Model.ChangeEvent.ChangeAction;
import net.sf.borg.model.entity.SyncableEntity.ObjectType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

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

package net.sf.borg.model.undo;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.beans.Subtask;

public class SubtaskUndoItem extends UndoItem<Subtask> {

	@Override
	public void executeUndo() {
		try {
			if (action == actionType.DELETE) {
				TaskModel.getReference().saveSubTask(item, true);
			} else if (action == actionType.UPDATE) {
				TaskModel.getReference().saveSubTask(item, true);
			} else if (action == actionType.ADD) {
				TaskModel.getReference().deleteSubTask(item.getId(),true);
			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	private SubtaskUndoItem() {

	}

	static private String itemString(Subtask st) {
		return st.getDescription();
	}

	public static SubtaskUndoItem recordUpdate(Subtask subtask) {
		SubtaskUndoItem undoItem = new SubtaskUndoItem();
		undoItem.item = subtask;
		undoItem.action = actionType.UPDATE;
		undoItem.setDescription(Resource.getPlainResourceString("Change") + " "
				+ Resource.getPlainResourceString("subtask") + " "
				+ itemString(subtask));
		
		TaskUndoItem ti = TaskUndoItem.getLastTaskItem();
		if( ti != null )
			ti.addSubtask(undoItem);
		return undoItem;
	}

	public static SubtaskUndoItem recordAdd(Subtask subtask) {
		SubtaskUndoItem undoItem = new SubtaskUndoItem();
		undoItem.item = subtask;
		undoItem.action = actionType.ADD;
		undoItem.setDescription(Resource.getPlainResourceString("Add") + " "
				+ Resource.getPlainResourceString("subtask") + " "
				+ itemString(subtask));
		TaskUndoItem ti = TaskUndoItem.getLastTaskItem();
		if( ti != null )
			ti.addSubtask(undoItem);
		return undoItem;
	}

	public static SubtaskUndoItem recordDelete(Subtask subtask) {
		SubtaskUndoItem undoItem = new SubtaskUndoItem();
		undoItem.item = subtask;
		undoItem.action = actionType.DELETE;
		undoItem.setDescription(Resource.getPlainResourceString("Delete") + " "
				+ Resource.getPlainResourceString("subtask") + " "
				+ itemString(subtask));
		TaskUndoItem ti = TaskUndoItem.getLastTaskItem();
		if( ti != null )
			ti.addSubtask(undoItem);
		return undoItem;
	}

}

package net.sf.borg.model.db;

import java.sql.SQLException;
import java.util.Collection;

import net.sf.borg.model.Subtask;


public interface SubtaskDB {

    public Collection getSubTasks(int taskid) throws SQLException;

    public void deleteSubTask(int id) throws SQLException;

    public void addSubTask(Subtask s) throws SQLException;

}
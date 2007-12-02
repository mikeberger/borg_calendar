/*
 This file is part of BORG.
 
 BORG is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 BORG is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with BORG; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 Copyright 2003 by Mike Berger
 */
package net.sf.borg.model;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.Vector;

import net.sf.borg.common.DateUtil;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.NotSupportedWarning;
import net.sf.borg.common.Resource;
import net.sf.borg.common.Warning;
import net.sf.borg.common.XTree;
import net.sf.borg.model.beans.Project;
import net.sf.borg.model.beans.ProjectXMLAdapter;
import net.sf.borg.model.beans.Subtask;
import net.sf.borg.model.beans.SubtaskXMLAdapter;
import net.sf.borg.model.beans.Task;
import net.sf.borg.model.beans.TaskXMLAdapter;
import net.sf.borg.model.beans.Tasklog;
import net.sf.borg.model.beans.TasklogXMLAdapter;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.db.BeanDataFactoryFactory;
import net.sf.borg.model.db.DBException;
import net.sf.borg.model.db.IBeanDataFactory;
import net.sf.borg.model.db.TaskDB;
import net.sf.borg.model.db.jdbc.JdbcDB;

public class TaskModel extends Model implements Model.Listener, Transactional {

    private BeanDB db_; // the database

    public BeanDB getDB() {
	return (db_);
    }

    // map of tasks keyed by day - for performance
    private HashMap btmap_;

    private Vector allmap_;

    private HashMap stmap_;

    private HashMap pmap_;

    private TaskTypes taskTypes_ = new TaskTypes();

    public LinkedList get_tasks(int daykey) {
	return ((LinkedList) btmap_.get(new Integer(daykey)));
    }

    public LinkedList get_subtasks(int daykey) {
	return ((LinkedList) stmap_.get(new Integer(daykey)));
    }

    public LinkedList get_projects(int daykey) {
	return ((LinkedList) pmap_.get(new Integer(daykey)));
    }

    public Vector get_tasks() {
	return (allmap_);
    }

    private TaskModel() {
	btmap_ = new HashMap();
	stmap_ = new HashMap();
	pmap_ = new HashMap();
	allmap_ = new Vector();
    }

    static private TaskModel self_ = null;

    static public TaskModel getReference() {
	return (self_);
    }

    public static TaskModel create() {
	self_ = new TaskModel();
	return (self_);
    }

    public void remove() {
	removeListeners();
	try {
	    if (db_ != null)
		db_.close();
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	    return;
	}
	db_ = null;
    }

    public TaskTypes getTaskTypes() {
	return (taskTypes_);
    }

    public void saveTaskTypes(TaskTypes tt) throws Exception {
	if (tt != null) {
	    tt.validate();
	    taskTypes_ = tt.copy();
	}
	db_.setOption(new BorgOption("SMODEL", taskTypes_.toString()));
    }

    public Collection getCategories() throws Exception, DBException {

	TreeSet categories = new TreeSet();
	Iterator itr = db_.readAll().iterator();
	while (itr.hasNext()) {
	    Task t = (Task) itr.next();
	    String cat = t.getCategory();
	    if (cat != null && !cat.equals(""))
		categories.add(cat);
	}

	try {
	    Collection projects = getProjects();
	    itr = projects.iterator();
	    while (itr.hasNext()) {
		Project t = (Project) itr.next();
		String cat = t.getCategory();
		if (cat != null && !cat.equals(""))
		    categories.add(cat);
	    }
	} catch (Exception e) {
	    // ignore this one
	}
	return (categories);

    }

    public Collection getTasks() throws DBException, Exception {
	return db_.readAll();
    }

    // load a map of tasks to day keys for performance. this is because the
    // month views will
    // repeatedly need to retrieve tasks per day and it would be slow to
    // repeatedly traverse
    // the entire task DB looking for them
    // only tasks that are viewed by day need to be cached here - those that
    // are not CLOSED and
    // have a due date - a small fraction of the total
    private void load_map() {

	// clear map
	btmap_.clear();
	allmap_.clear();
	stmap_.clear();
	pmap_.clear();
	

	try {

	    // iterate through tasks using taskmodel
	    Collection tasks = getTasks();
	    Iterator ti = tasks.iterator();
	    while (ti.hasNext()) {
		Task mr = (Task) ti.next();

		// for each task, get state and skip CLOSED or PR tasks
		if( isClosed(mr)) continue;

		if (!CategoryModel.getReference().isShown(mr.getCategory()))
		    continue;

		// use task due date to build a day key
		Date due = mr.getDueDate();
		if (due == null)
		    continue;

		GregorianCalendar g = new GregorianCalendar();
		g.setTime(due);
		int key = AppointmentModel.dkey(g);

		// add the task string to the btmap_
		// add the task to the mrs_ Vector. This is used by the todo gui
		Object o = btmap_.get(new Integer(key));
		if (o == null) {
		    o = new LinkedList();
		    btmap_.put(new Integer(key), o);
		}

		LinkedList l = (LinkedList) o;
		l.add(mr);
		allmap_.add(mr);
	    }

	    if (db_ instanceof TaskDB) {

		Collection projects = getProjects();
		ti = projects.iterator();
		while (ti.hasNext()) {
		    Project pj = (Project) ti.next();
		    if( pj.getDueDate() == null )
			continue;

		    if (pj.getStatus().equals(
			    Resource.getPlainResourceString("CLOSED")))
			continue;

		    if (!CategoryModel.getReference().isShown(pj.getCategory()))
			continue;

		    // use task due date to build a day key
		    Date due = pj.getDueDate();
		    GregorianCalendar g = new GregorianCalendar();
		    g.setTime(due);
		    int key = AppointmentModel.dkey(g);

		    // add the string to the btmap_
		    Object o = pmap_.get(new Integer(key));
		    if (o == null) {
			o = new LinkedList();
			pmap_.put(new Integer(key), o);
		    }

		    LinkedList l = (LinkedList) o;
		    l.add(pj);
		}

		Collection subtasks = getSubTasks();
		ti = subtasks.iterator();
		while (ti.hasNext()) {
		    Subtask st = (Subtask) ti.next();

		    if (st.getCloseDate() != null || st.getDueDate() == null)
			continue;

		    Task mr = getMR(st.getTask().intValue());
		    String cat = mr.getCategory();
		    if (cat == null || cat.equals(""))
			cat = CategoryModel.UNCATEGORIZED;

		    if (!CategoryModel.getReference().isShown(cat))
			continue;

		    // use task due date to build a day key
		    Date due = st.getDueDate();
		    GregorianCalendar g = new GregorianCalendar();
		    g.setTime(due);
		    int key = AppointmentModel.dkey(g);

		    // add the string to the btmap_
		    Object o = stmap_.get(new Integer(key));
		    if (o == null) {
			o = new LinkedList();
			stmap_.put(new Integer(key), o);
		    }

		    LinkedList l = (LinkedList) o;
		    l.add(st);
		}
	    }

	} catch (DBException e) {
	    if (e.getRetCode() != DBException.RET_NOT_FOUND) {
		Errmsg.errmsg(e);
		return;
	    }
	} catch (Exception e) {

	    Errmsg.errmsg(e);
	    return;
	}

    }

    // open the database
    public void open_db(String url, String username, 
	    boolean shared) throws Exception {

	StringBuffer tmp = new StringBuffer(url);
	IBeanDataFactory factory = BeanDataFactoryFactory.getInstance()
		.getFactory(tmp, shared);
	url = tmp.toString();
	// let the factory tweak dbdir

	db_ = factory.create(Task.class, url, username);

	// get XML that models states/transitions
	// set to default if it does not exist
	// see SMDB.java for the use of SMDB options
	String sm = db_.getOption("SMODEL");
	if (sm == null) {
	    try {
		// load XML from a file in the JAR
		// System.out.println("Loading default task model");
		taskTypes_.loadDefault();
		sm = taskTypes_.toString();
		db_.setOption(new BorgOption("SMODEL", sm));
	    } catch (NoSuchMethodError nsme) {
		// running in a Palm conduit under JRE 1.3
		// ignore
	    } catch (Exception e) {
		Errmsg.errmsg(e);
		return;
	    }
	} else {
	    taskTypes_.fromString(sm);
	}

	CategoryModel.getReference().addAll(getCategories());
	CategoryModel.getReference().addListener(this);

	load_map();

    }

    // delete a given task from the DB
    public void delete(int tasknum) throws Exception {

	try {
	    // have cascaded delete - don't need this
	    // Collection subtasks = getSubTasks(tasknum);
	    // Iterator it = subtasks.iterator();
	    // while (it.hasNext()) {
	    // Subtask st = (Subtask) it.next();
	    // deleteSubTask(st.getId().intValue());
	    // }
	    db_.delete(tasknum);
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}

	// have the borg class reload the calendar app side of things - so this
	// is one model
	// notifying the other of a change through the controller
	load_map();

	refreshListeners();

    }

    public void deleteProject(int id) throws Exception {

	try {
	    if (db_ instanceof TaskDB == false)
		throw new NotSupportedWarning(Resource
			.getPlainResourceString("SubtaskNotSupported"));
	    TaskDB sdb = (TaskDB) db_;
	    sdb.deleteProject(id);
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}

	// have the borg class reload the calendar app side of things - so this
	// is one model
	// notifying the other of a change through the controller
	load_map();

	refreshListeners();

    }

    // save a task from a filled in task Row
    public void savetask(Task task) throws Exception {

	// validations
	if( task.getProject() != null )
	{
	    Project p = TaskModel.getReference().getProject(task.getProject().intValue());
	    if( p != null && task.getDueDate() != null && p.getDueDate() != null && DateUtil.isAfter(task.getDueDate(),p.getDueDate()))
	    {
		throw new Warning(Resource
			.getPlainResourceString("taskdd_warning"));
	    }
	}
	
	
	// add task to DB
	Integer num = task.getTaskNumber();

	// if the task number is -1, it is a new task so
	// get a new task number.
	if (num.intValue() == -1) {
	    int newkey = db_.nextkey();
	    task.setKey(newkey);
	    task.setTaskNumber(new Integer(newkey));
	    try {
		db_.addObj(task, false);
	    } catch (DBException e) {
		Errmsg.errmsg(e);
	    }
	} else {
	    // task exists - so update existing task in DB
	    try {
		// update close date
		if (task.getState() != null
			&& isClosed(task))
		    task.setCD(new Date());
		int key = task.getTaskNumber().intValue();
		task.setKey(key);
		db_.updateObj(task, false);
	    } catch (DBException e) {
		Errmsg.errmsg(e);
	    }
	}

	// have the borg class reload the calendar app side of things - so this
	// is one model
	// notifying the other of a change through the controller
	load_map();

	// inform views of data change
	refreshListeners();

    }

    // allocate a new Row from the DB
    public Task newMR() {
	return ((Task) db_.newObj());
    }

    // read a task from the DB by task number
    public Task getMR(int num) throws DBException, Exception {
	return ((Task) db_.readObj(num));
    }

    // force a task to be updated to CLOSED state and saved
    public void close(int num) throws Exception, Warning {

	Collection sts = TaskModel.getReference().getSubTasks(num);
	Iterator it = sts.iterator();
	while (it.hasNext()) {
	    Subtask st = (Subtask) it.next();
	    if (st.getCloseDate() == null) {
		throw new Warning(Resource.getResourceString("open_subtasks"));
	    }
	}

	Task task = getMR(num);
	task.setState(TaskModel.getReference().getTaskTypes().getFinalState(
		task.getType()));
	savetask(task);
    }

    public void closeProject(int num) throws Exception, Warning {

	Project p = getProject(num);
	p.setStatus(Resource.getPlainResourceString("CLOSED"));
	saveProject(p);
    }

    // export the task data for all tasks to XML
    public void export(Writer fw) throws Exception {

	// FileWriter fw = new FileWriter(fname);
	fw.write("<TASKS>\n");
	TaskXMLAdapter ta = new TaskXMLAdapter();

	// export options
	try {
	    Collection opts = db_.getOptions();
	    Iterator opiter = opts.iterator();
	    while (opiter.hasNext()) {
		BorgOption option = (BorgOption) opiter.next();
		XTree xt = new XTree();
		xt.name("OPTION");
		xt.appendChild(option.getKey(), option.getValue());
		fw.write(xt.toString());
	    }
	} catch (DBException e) {
	    if (e.getRetCode() != DBException.RET_NOT_FOUND)
		Errmsg.errmsg(e);
	}

	ProjectXMLAdapter pa = new ProjectXMLAdapter();

	// export projects
	if (TaskModel.getReference().hasSubTasks()) {
	    try {

		Collection projects = getProjects();
		Iterator ti = projects.iterator();
		while (ti.hasNext()) {
		    Project p = (Project) ti.next();

		    XTree xt = pa.toXml(p);
		    fw.write(xt.toString());
		}
	    } catch (DBException e) {
		if (e.getRetCode() != DBException.RET_NOT_FOUND)
		    Errmsg.errmsg(e);
	    }
	}
	// export tasks
	try {

	    Collection tasks = getTasks();
	    Iterator ti = tasks.iterator();
	    while (ti.hasNext()) {
		Task task = (Task) ti.next();

		XTree xt = ta.toXml(task);
		fw.write(xt.toString());
	    }
	} catch (DBException e) {
	    if (e.getRetCode() != DBException.RET_NOT_FOUND)
		Errmsg.errmsg(e);
	}

	SubtaskXMLAdapter sta = new SubtaskXMLAdapter();

	// export subtasks
	if (TaskModel.getReference().hasSubTasks()) {
	    try {

		Collection subtasks = getSubTasks();
		Iterator ti = subtasks.iterator();
		while (ti.hasNext()) {
		    Subtask stask = (Subtask) ti.next();

		    XTree xt = sta.toXml(stask);
		    fw.write(xt.toString());
		}
	    } catch (DBException e) {
		if (e.getRetCode() != DBException.RET_NOT_FOUND)
		    Errmsg.errmsg(e);
	    }

	    TasklogXMLAdapter tla = new TasklogXMLAdapter();

	    // export tasklogs
	    try {

		Collection logs = getLogs();
		Iterator ti = logs.iterator();
		while (ti.hasNext()) {
		    Tasklog tlog = (Tasklog) ti.next();

		    XTree xt = tla.toXml(tlog);
		    fw.write(xt.toString());
		}
	    } catch (DBException e) {
		if (e.getRetCode() != DBException.RET_NOT_FOUND)
		    Errmsg.errmsg(e);
	    }
	}
	fw.write("</TASKS>");

    }

    public Collection getProjects() throws Exception {
	if (db_ instanceof TaskDB == false)
	    return new ArrayList();
	TaskDB sdb = (TaskDB) db_;
	return sdb.getProjects();
    }

    public Project getProject(int id) throws Exception {
	if (db_ instanceof TaskDB == false)
	    throw new NotSupportedWarning(Resource
		    .getPlainResourceString("SubtaskNotSupported"));
	TaskDB sdb = (TaskDB) db_;
	return sdb.getProject(id);
    }

    // export the task data to a file in XML
    public void importXml(XTree xt) throws Exception {

	TaskXMLAdapter aa = new TaskXMLAdapter();
	SubtaskXMLAdapter sa = new SubtaskXMLAdapter();
	TasklogXMLAdapter la = new TasklogXMLAdapter();
	ProjectXMLAdapter pa = new ProjectXMLAdapter();
	
	JdbcDB.execSQL("SET REFERENTIAL_INTEGRITY FALSE;"); 


	// for each appt - create an Appointment and store
	for (int i = 1;; i++) {
	    XTree ch = xt.child(i);
	    if (ch == null)
		break;

	    if (ch.name().equals("OPTION")) {
		XTree opt = ch.child(1);
		if (opt == null)
		    continue;

		if (opt.name().equals("SMODEL")) {
		    taskTypes_.fromString(opt.value());
		    db_.setOption(new BorgOption("SMODEL", taskTypes_
			    .toString()));

		} else {
		    db_.setOption(new BorgOption(opt.name(), opt.value()));
		}
	    }

	    else if (ch.name().equals("Task")) {
		Task task = (Task) aa.fromXml(ch);
		if (task.getPriority() == null)
		    task.setPriority(new Integer(3));
		try {
		    db_.addObj(task, false);

		    if (TaskModel.getReference().hasSubTasks()) {
			// migrate from old subtask mechanism
			if (!isClosed(task)
				&& task.getTodoList() != null
				&& task.getUserTask1() != null) {
			    // add system subtasks
			    String todos = task.getTodoList();
			    String cbs[] = TaskModel.getReference()
				    .getTaskTypes().checkBoxes(task.getType());
			    for (int sti = 0; sti < cbs.length; sti++) {
				if (!cbs[sti].equals(TaskTypes.NOCBVALUE)) {
				    Subtask st = new Subtask();
				    st.setStartDate(new Date());
				    st.setDescription(cbs[sti]);
				    st.setTask(task.getTaskNumber());
				    if (todos.indexOf(Integer.toString(sti)) != -1) {
					st.setCloseDate(new Date());
				    }
				    saveSubTask(st);
				}
			    }
			    if (task.getUserTask1() != null) {
				Subtask st = new Subtask();
				st.setStartDate(new Date());
				st.setDescription(task.getUserTask1());
				st.setTask(task.getTaskNumber());
				if (todos.indexOf("6") != -1) {
				    st.setCloseDate(new Date());
				}
				saveSubTask(st);
			    }
			    if (task.getUserTask2() != null) {
				Subtask st = new Subtask();
				st.setStartDate(new Date());
				st.setDescription(task.getUserTask2());
				st.setTask(task.getTaskNumber());
				if (todos.indexOf("7") != -1) {
				    st.setCloseDate(new Date());
				}
				saveSubTask(st);
			    }
			    if (task.getUserTask3() != null) {
				Subtask st = new Subtask();
				st.setStartDate(new Date());
				st.setDescription(task.getUserTask3());
				st.setTask(task.getTaskNumber());
				if (todos.indexOf("8") != -1) {
				    st.setCloseDate(new Date());
				}
				saveSubTask(st);
			    }
			    if (task.getUserTask4() != null) {
				Subtask st = new Subtask();
				st.setStartDate(new Date());
				st.setDescription(task.getUserTask4());
				st.setTask(task.getTaskNumber());
				if (todos.indexOf("9") != -1) {
				    st.setCloseDate(new Date());
				}
				saveSubTask(st);
			    }
			    if (task.getUserTask5() != null) {
				Subtask st = new Subtask();
				st.setStartDate(new Date());
				st.setDescription(task.getUserTask5());
				st.setTask(task.getTaskNumber());
				if (todos.indexOf("A") != -1) {
				    st.setCloseDate(new Date());
				}
				saveSubTask(st);
			    }
			    task.setTodoList(null);
			    task.setUserTask1(null);
			    task.setUserTask2(null);
			    task.setUserTask3(null);
			    task.setUserTask4(null);
			    task.setUserTask5(null);
			    savetask(task);
			}
		    }
		} catch (DBException e) {
		    if (e.getRetCode() == DBException.RET_DUPLICATE) {
			task.setTaskNumber(new Integer(-1));
			savetask(task);

		    } else {
			throw e;
		    }
		}
	    }

	    else if (ch.name().equals("Subtask")) {
		Subtask subtask = (Subtask) sa.fromXml(ch);
		try {
		    subtask.setId(null);
		    saveSubTask(subtask);
		} catch (Exception e) {
		    Errmsg.errmsg(e);
		}
	    }

	    else if (ch.name().equals("Tasklog")) {
		Tasklog tlog = (Tasklog) la.fromXml(ch);
		try {
		    tlog.setId(null);
		    saveLog(tlog);
		} catch (Exception e) {
		    Errmsg.errmsg(e);
		}
	    } else if (ch.name().equals("Project")) {
		Project p = (Project) pa.fromXml(ch);
		try {
		    TaskDB sdb = (TaskDB) db_;
		    sdb.addProject(p);
		} catch (Exception e) {
		    Errmsg.errmsg(e);
		}
	    }
	}
	JdbcDB.execSQL("SET REFERENTIAL_INTEGRITY TRUE;"); 
	// refresh all views that are displaying appt data from this model
	load_map();
	refreshListeners();

    }

    public void sync() throws DBException {
	db_.sync();
	load_map();
	refreshListeners();
    }

    public void close_db() throws Exception {
	db_.close();
    }

    /*
         * (non-Javadoc)
         * 
         * @see net.sf.borg.model.Model.Listener#refresh()
         */
    public void refresh() {
	try {
	    load_map();
	    refreshListeners();
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}

    }

    public Collection getSubTasks(int taskid) throws Exception {
	if (db_ instanceof TaskDB == false)
	    throw new NotSupportedWarning(Resource
		    .getPlainResourceString("SubtaskNotSupported"));

	TaskDB sdb = (TaskDB) db_;
	return sdb.getSubTasks(taskid);

    }

    public Collection getSubTasks() throws Exception {
	if (db_ instanceof TaskDB == false)
	    throw new NotSupportedWarning(Resource
		    .getPlainResourceString("SubtaskNotSupported"));

	TaskDB sdb = (TaskDB) db_;
	return sdb.getSubTasks();

    }

    public Collection getTasks(int projectid) throws Exception {
	if (db_ instanceof TaskDB == false)
	    throw new NotSupportedWarning(Resource
		    .getPlainResourceString("SubtaskNotSupported"));

	TaskDB sdb = (TaskDB) db_;
	return sdb.getTasks(projectid);

    }
    
    public Collection getSubProjects(int projectid) throws Exception {
	if (db_ instanceof TaskDB == false)
	    throw new NotSupportedWarning(Resource
		    .getPlainResourceString("SubtaskNotSupported"));

	TaskDB sdb = (TaskDB) db_;
	return sdb.getSubProjects(projectid);

    }
    
    public Collection getAllSubProjects(int projectid) throws Exception {
	Collection c = new ArrayList();
	if (db_ instanceof TaskDB == false)
	    throw new NotSupportedWarning(Resource
		    .getPlainResourceString("SubtaskNotSupported"));
	addSubProjectsToCollection( c, projectid);
	return c;
    }
    
    private void addSubProjectsToCollection(Collection c, int projectid) throws Exception
    {

	// add my children
	Collection children = getSubProjects(projectid);
	if( children.isEmpty()) return;
	c.addAll(children);
	
	// add my children's children
	Iterator it = children.iterator();
	while( it.hasNext())
	{
	    Project p = (Project) it.next();
	    addSubProjectsToCollection( c, p.getId().intValue());
	}

    }

    public void deleteSubTask(int id) throws Exception {
	if (db_ instanceof TaskDB == false)
	    throw new NotSupportedWarning(Resource
		    .getPlainResourceString("SubtaskNotSupported"));

	TaskDB sdb = (TaskDB) db_;
	sdb.deleteSubTask(id);
	load_map();
	refreshListeners();
    }

    public void saveSubTask(Subtask s) throws Exception {
	if (db_ instanceof TaskDB == false)
	    throw new NotSupportedWarning(Resource
		    .getPlainResourceString("SubtaskNotSupported"));

	TaskDB sdb = (TaskDB) db_;
	if (s.getId() == null || s.getId().intValue() <= 0) {
	    s.setId(new Integer(sdb.nextSubTaskKey()));
	    sdb.addSubTask(s);
	} else {
	    sdb.updateSubTask(s);
	}

	load_map();
	refreshListeners();
    }

    public void addLog(int taskid, String desc) throws Exception {
	if (db_ instanceof TaskDB == false)
	    return;
	// throw new Exception(Resource
	// .getPlainResourceString("SubtaskNotSupported"));
	TaskDB sdb = (TaskDB) db_;
	sdb.addLog(taskid, desc);
    }

    public void saveLog(Tasklog tlog) throws Exception {
	if (db_ instanceof TaskDB == false)
	    throw new NotSupportedWarning(Resource
		    .getPlainResourceString("SubtaskNotSupported"));
	TaskDB sdb = (TaskDB) db_;
	sdb.saveLog(tlog);
    }

    public Collection getLogs(int taskid) throws Exception {
	if (db_ instanceof TaskDB == false)
	    throw new NotSupportedWarning(Resource
		    .getPlainResourceString("SubtaskNotSupported"));
	TaskDB sdb = (TaskDB) db_;
	return sdb.getLogs(taskid);
    }

    public Collection getLogs() throws Exception {
	if (db_ instanceof TaskDB == false)
	    throw new NotSupportedWarning(Resource
		    .getPlainResourceString("SubtaskNotSupported"));
	TaskDB sdb = (TaskDB) db_;
	return sdb.getLogs();
    }

    public static int daysLeft(Date dd) {

	if (dd == null)
	    return 0;
	Calendar today = new GregorianCalendar();
	Calendar dcal = new GregorianCalendar();
	dcal.setTime(dd);

	// find days left
	int days = 0;
	if (dcal.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
	    days = dcal.get(Calendar.DAY_OF_YEAR)
		    - today.get(Calendar.DAY_OF_YEAR);
	} else {
	    days = new Long((dd.getTime() - today.getTime().getTime())
		    / (1000 * 60 * 60 * 24)).intValue();
	}

	// if due date is past, set days left to 0
	// negative days are silly
	if (days < 0)
	    days = 0;
	return days;
    }
    
    public static int daysBetween(Date start, Date dd) {

	if (dd == null)
	    return 0;
	Calendar startcal = new GregorianCalendar();
	Calendar dcal = new GregorianCalendar();
	dcal.setTime(dd);
	startcal.setTime(start);

	// find days left
	int days = 0;
	if (dcal.get(Calendar.YEAR) == startcal.get(Calendar.YEAR)) {
	    days = dcal.get(Calendar.DAY_OF_YEAR)
		    - startcal.get(Calendar.DAY_OF_YEAR);
	} else {
	    days = new Long((dd.getTime() - startcal.getTime().getTime())
		    / (1000 * 60 * 60 * 24)).intValue();
	}

	// if due date is past, set days left to 0
	// negative days are silly
	if (days < 0)
	    days = 0;
	return days;
    }

    public boolean hasSubTasks() {
	return db_ instanceof TaskDB;
    }

    public void beginTransaction() throws Exception {
	if (db_ instanceof Transactional) {
	    Transactional t = (Transactional) db_;
	    t.beginTransaction();
	}

    }

    public void commitTransaction() throws Exception {
	if (db_ instanceof Transactional) {
	    Transactional t = (Transactional) db_;
	    t.commitTransaction();
	}
    }

    public void rollbackTransaction() throws Exception {
	if (db_ instanceof Transactional) {
	    Transactional t = (Transactional) db_;
	    t.rollbackTransaction();
	}
    }

    public void saveProject(Project p) throws Exception {
	if (db_ instanceof TaskDB == false)
	    throw new NotSupportedWarning(Resource
		    .getPlainResourceString("SubtaskNotSupported"));
	
	// validation that task due dates are before project due date
	if( p.getId() != null && p.getId().intValue() != -1)
	{
	    Collection tasks = TaskModel.getReference().getTasks(p.getId().intValue());
	    Iterator it = tasks.iterator();
	    while( it.hasNext())
	    {
		Task t = (Task) it.next();
		if( p.getDueDate() != null && t.getDueDate() != null && !TaskModel.isClosed(t) && DateUtil.isAfter( t.getDueDate(), p.getDueDate()))
		{
		    throw new Warning(Resource.getPlainResourceString("projdd_warning")+": "+t.getTaskNumber());
		}
	    }
	    
	    Collection children = TaskModel.getReference().getSubProjects(p.getId().intValue());
	    Iterator it2 = children.iterator();
	    while( it2.hasNext())
	    {
		Project child = (Project) it2.next();
		if( p.getDueDate() != null && child.getDueDate() != null && !TaskModel.isClosed(child) && DateUtil.isAfter( child.getDueDate(), p.getDueDate()))
		{
		    throw new Warning(Resource.getPlainResourceString("projchild_warning")+": "+ child.getId().intValue());
		}
	    }
	}
	
	// validate against parent
	if( p.getParent() != null )
	{
	    Project par = TaskModel.getReference().getProject(p.getParent().intValue());
	    if( par != null )
	    {
		if( p.getDueDate() != null && par.getDueDate() != null && DateUtil.isAfter( p.getDueDate(), par.getDueDate()))
		{
		    throw new Warning(Resource.getPlainResourceString("projpar_warning"));
		}
	    }
	}
	   

	if (p.getId() != null && p.getStatus().equals(Resource.getPlainResourceString("CLOSED"))) {
	    // make sure that all tasks are closed
	    Collection ptasks = TaskModel.getReference().getTasks(
		    p.getId().intValue());

	    Iterator it = ptasks.iterator();
	    while (it.hasNext()) {
		Task pt = (Task) it.next();
		if (!isClosed(pt)) {
		    throw new Warning(Resource
			    .getPlainResourceString("close_proj_warn"));
		}
	    }
	}

	TaskDB sdb = (TaskDB) db_;
	if (p.getId() == null || p.getId().intValue() <= 0) {
	    p.setId(new Integer(sdb.nextProjectKey()));
	    sdb.addProject(p);
	} else {
	    sdb.updateProject(p);
	}

	load_map();
	refreshListeners();
    }
    
    static public boolean isClosed(Task t)
    {
	String stat = t.getState();
	String type = t.getType();
	return stat.equals(TaskModel.getReference().getTaskTypes().getFinalState(type));
    }
    
    static public boolean isClosed(Project p)
    {
	String stat = p.getStatus();
	return stat.equals(Resource.getPlainResourceString("CLOSED"));
    }
}

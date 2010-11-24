package net.sf.borg.plugin.reports;
/*
 * This file is part of BORG.
 *
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright 2003 by Mike Berger
 */


import java.util.Collection;
import java.util.Date;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Warning;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Subtask;
import net.sf.borg.plugin.common.Resource;
import net.sf.borg.ui.View;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;

/**
 * GanttFrame generates a Gantt chart for tasks using JFreechart
 * 
 */
class GanttFrame extends View {

	private static final long serialVersionUID = 1L;
	
	/** The GANTT chart size. */
	static private PrefName GANTTSIZE = new PrefName("ganttsize",
			"-1,-1,-1,-1,N");

	/**
	 * add an item to the chart
	 * 
	 * @param s
	 *            the task series
	 * @param name
	 *            the item name
	 * @param start
	 *            the start date
	 * @param end
	 *            the end date
	 * @throws Warning
	 */
	private static void addChartItem(TaskSeries s, String name, Date start,
			Date end) throws Warning {
		if (start == null || end == null)
			return;
		try {
			s.add(new Task(name, new SimpleTimePeriod(start, end)));
		} catch (IllegalArgumentException e) {
			throw new Warning(Resource.getResourceString("TimePeriodException")
					+ name);
		}
	}

	/**
	 * add a project to the chart. Includes tasks and optionally subtasks
	 * 
	 * @param p
	 *            the Project
	 * @param sched
	 *            the task series (scheduled)
	 * @param actual
	 *            the task series (actual)
	 * @throws Warning
	 * @throws Exception
	 */
	private static void addProject(Project p, TaskSeries sched,
			TaskSeries actual) throws Warning, Exception {

		// add the project
		addChartItem(sched, Resource.getResourceString("project") + ": "
				+ p.getDescription(), p.getStartDate(), p.getDueDate());

		// add tasks
		Collection<net.sf.borg.model.entity.Task> tasks = TaskModel
				.getReference().getTasks(p.getKey());
		for (net.sf.borg.model.entity.Task task : tasks) {
			Date dd = task.getDueDate();

			// if no task duedate - use projects
			if (dd == null)
				dd = p.getDueDate();

			// add task to chart
			String tlabel = Integer.toString(task.getKey()) + "-"
					+ task.getDescription();
			addChartItem(sched, tlabel, task.getStartDate(), dd);

			// if task is closed - add actual dates
			if (TaskModel.isClosed(task))
				addChartItem(actual, tlabel, task.getStartDate(), task
						.getCompletionDate());

			// check if option to show subtasks is set
			String show_st = Prefs.getPref(PrefName.GANTT_SHOW_SUBTASKS);
			if (show_st.equals("true")) {

				// add subtasks
				Collection<Subtask> subtasks = TaskModel.getReference()
						.getSubTasks(task.getKey());
				for (Subtask subtask : subtasks) {

					// use dates of parent objects if subtask lacks dates
					dd = subtask.getDueDate();
					if (dd == null)
						dd = task.getDueDate();
					if (dd == null)
						dd = p.getDueDate();
					Date sd = subtask.getStartDate();
					if (sd == null)
						sd = task.getStartDate();
					if (sd == null)
						sd = p.getStartDate();

					// add subtask
					addChartItem(sched, subtask.getDescription(), sd, dd);

					// if subtask is closed - add actual dates
					if (subtask.getCloseDate() != null) {
						addChartItem(actual, subtask.getDescription(), sd,
								subtask.getCloseDate());
					}
				}
			}

		}

		// if this project has subprojects, add them
		Collection<Project> subprojects = TaskModel.getReference()
				.getSubProjects(p.getKey());
		for (Project subproject : subprojects) {
			addProject(subproject, sched, actual);
		}

	}

	/**
	 * create the data items that JFree needs for the chart
	 * 
	 * @param p
	 *            the Project
	 * @return some JFree thing
	 * @throws Exception
	 * @throws Warning
	 */
	private static IntervalCategoryDataset createDataset(Project p)
			throws Exception, Warning {

		// create schedule and actual series
		final TaskSeries sched = new TaskSeries(Resource
				.getResourceString("Scheduled"));
		final TaskSeries actual = new TaskSeries(Resource
				.getResourceString("Actual"));

		// add the project items to the series
		addProject(p, sched, actual);

		// create another jfree object
		final TaskSeriesCollection collection = new TaskSeriesCollection();
		collection.add(sched);
		collection.add(actual);

		return collection;
	}

	/**
	 * show a gantt chart for a project
	 * 
	 * @param p
	 *            the project
	 * @throws Exception
	 * @throws Warning
	 */
	public static void showChart(Project p) throws Exception, Warning {
		final GanttFrame chartFrame = new GanttFrame(p);
		chartFrame.pack();
		chartFrame.setVisible(true);
		chartFrame.manageMySize(GANTTSIZE);
	}

	/**
	 * constructor
	 * 
	 * @param p
	 *            the project
	 * @throws Exception
	 * @throws Warning
	 */
	private GanttFrame(Project p) throws Exception, Warning {

		super();

		// load the project data into JFree objects
		final IntervalCategoryDataset dataset = createDataset(p);

		// create the chart with Jfree apis
		final JFreeChart chart = ChartFactory.createGanttChart(p.getDescription(), "",
				Resource.getResourceString("Date"), dataset, true, // include legend
				true, // tooltips
				false // urls
				);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

		// add the JFree panel to this window
		getContentPane().add(chartPanel);

	}


	@Override
	public void destroy() {
		this.dispose();

	}

	@Override
	public void refresh() {
	  // empty
	}

	@Override
	public void update(ChangeEvent event) {
		refresh();
	}

}

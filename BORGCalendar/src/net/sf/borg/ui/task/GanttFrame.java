package net.sf.borg.ui.task;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.common.Warning;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.beans.Project;
import net.sf.borg.model.beans.Subtask;
import net.sf.borg.ui.View;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;

public class GanttFrame extends View {

    public static void showChart(Project p) throws Exception, Warning {
	final GanttFrame chartFrame = new GanttFrame(p);
	chartFrame.pack();
	chartFrame.setVisible(true);
	chartFrame.manageMySize(PrefName.GANTTSIZE);
    }

    private static void addChartItem(TaskSeries s, String name, Date start,
	    Date end) throws Warning {
	if (start == null || end == null)
	    return;
	try {
	    s.add(new Task(name, new SimpleTimePeriod(start, end)));
	} catch (IllegalArgumentException e) {
	    throw new Warning(Resource
		    .getPlainResourceString("TimePeriodException")
		    + name);
	}
    }

    private static IntervalCategoryDataset createDataset(Project p)
	    throws Exception, Warning {

	final TaskSeries sched = new TaskSeries(Resource
		.getPlainResourceString("Scheduled"));
	final TaskSeries actual = new TaskSeries(Resource
		.getPlainResourceString("Actual"));

	addChartItem(sched, Resource.getPlainResourceString("project") + ": "
		+ p.getDescription(), p.getStartDate(), p.getDueDate());
	Collection tasks = TaskModel.getReference().getTasks(
		p.getId().intValue());
	Iterator it = tasks.iterator();
	while (it.hasNext()) {
	    net.sf.borg.model.beans.Task t = (net.sf.borg.model.beans.Task) it
		    .next();
	    Date dd = t.getDueDate();

	    if (dd == null)
		dd = p.getDueDate();
	    String tlabel = Integer.toString(t.getTaskNumber().intValue()) + "-" + t.getDescription();
	    addChartItem(sched, tlabel, t.getStartDate(), dd);
	    if (TaskModel.isClosed(t))
		addChartItem(actual, tlabel, t.getStartDate(), t
			.getCD());
	    
	    String show_st = Prefs.getPref(PrefName.GANTT_SHOW_SUBTASKS);
	    if( show_st.equals("true"))
	    {
		Collection sts = TaskModel.getReference().getSubTasks(
			t.getTaskNumber().intValue());
		Iterator it2 = sts.iterator();
		while (it2.hasNext()) {
		    Subtask st = (Subtask) it2.next();
		    dd = st.getDueDate();
		    if (dd == null)
			dd = t.getDueDate();
		    if (dd == null)
			dd = p.getDueDate();
		    addChartItem(sched, st.getDescription(), t.getStartDate(), dd);
		    if (st.getCloseDate() != null) {
			addChartItem(actual, st.getDescription(), t.getStartDate(),
				st.getCloseDate());
		    }
		}
	    }
	}

	final TaskSeriesCollection collection = new TaskSeriesCollection();
	collection.add(sched);
	collection.add(actual);

	return collection;
    }

    private GanttFrame(Project p) throws Exception, Warning {

	super();

	final IntervalCategoryDataset dataset = createDataset(p);
	final JFreeChart chart = createChart(p.getDescription(), dataset);
	final ChartPanel chartPanel = new ChartPanel(chart);
	chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
	getContentPane().add(chartPanel);

    }

    public void destroy() {
	this.dispose();

    }

    public void refresh() {
	// TODO Auto-generated method stub

    }

    private JFreeChart createChart(String title, IntervalCategoryDataset dataset) {
	final JFreeChart chart = ChartFactory.createGanttChart(title, // chart
                                                                        // title
		"",// Resource.getPlainResourceString("Item"), // domain
                        // axis label
		Resource.getPlainResourceString("Date"), // range axis
                                                                // label
		dataset, // data
		true, // include legend
		true, // tooltips
		false // urls
		);

	return chart;
    }

}

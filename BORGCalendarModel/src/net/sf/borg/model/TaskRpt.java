package net.sf.borg.model;

import java.util.Collection;
import java.util.Iterator;
public class TaskRpt
{


	public static String report(TaskModel tm) throws Exception
	{

		StringBuffer sb = new StringBuffer();
		

		Collection tasks = tm.getTasks();
		Iterator it = tasks.iterator();
		while (it.hasNext())
		{
			Task t = (Task) it.next();
			
			String status = t.getState();

			if (status.equals("CLOSED") || status.equals("PR"))
				continue;
				
		    sb.append("----------------------------------------------------------------------\n");
		    sb.append("TASK: " + t.getTaskNumber() + "\n" );
		    sb.append("STATUS: " + t.getState() + "\n");
		    if( t.getDueDate() != null )
		    	sb.append( "DUEDATE: " + t.getDueDate() + "\n" );
		    	
		    sb.append("DESCRIPTION:\n" + t.getDescription() + "\n\n");
			String res = t.getResolution();
			if( res != null && !res.equals("")) 
			sb.append("RESOLUTION:\n" + t.getResolution() + "\n\n");
			
					
		}


		return( sb.toString());
	}
}

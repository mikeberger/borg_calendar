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
 
Copyright 2004 by ==Quiet==
 */
package net.sf.borgweb.form;


import java.text.DateFormat;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
 

// TODO - this should be generated code
public class TaskForm extends ActionForm {

	private String key_;
	public String getKey() {
		return key_;
	}
	public void setKey(String key_) {
		this.key_ = key_;
	}
	
	private String States_;
	public String getStates() { return( States_ ); }
	public void setStates( String xx ){ States_ = xx; }
	
	private String TaskNumber_;
	public String getTaskNumber() { return( TaskNumber_ ); }
	public void setTaskNumber( String xx ){ TaskNumber_ = xx; }

	private String StartDate_;
	public String getStartDate() { return( StartDate_ ); }
	public void setStartDate( String xx ){ StartDate_ = xx; }

	private String DueDate_;
	public String getDueDate() { return( DueDate_ ); }
	public void setDueDate( String xx ){ DueDate_ = xx; }

	private String PersonAssigned_;
	public String getPersonAssigned() { return( PersonAssigned_ ); }
	public void setPersonAssigned( String xx ){ PersonAssigned_ = xx; }

	private String Priority_;
	public String getPriority() { return( Priority_ ); }
	public void setPriority( String xx ){ Priority_ = xx; }

	private String State_;
	public String getState() { return( State_ ); }
	public void setState( String xx ){ State_ = xx; }

	private String Type_;
	public String getType() { return( Type_ ); }
	public void setType( String xx ){ Type_ = xx; }

	private String Description_;
	public String getDescription() { return( Description_ ); }
	public void setDescription( String xx ){ Description_ = xx; }

	private String Resolution_;
	public String getResolution() { return( Resolution_ ); }
	public void setResolution( String xx ){ Resolution_ = xx; }

	private String TodoList_;
	public String getTodoList() { return( TodoList_ ); }
	public void setTodoList( String xx ){ TodoList_ = xx; }

	private String UserTask1_;
	public String getUserTask1() { return( UserTask1_ ); }
	public void setUserTask1( String xx ){ UserTask1_ = xx; }

	private String UserTask2_;
	public String getUserTask2() { return( UserTask2_ ); }
	public void setUserTask2( String xx ){ UserTask2_ = xx; }

	private String UserTask3_;
	public String getUserTask3() { return( UserTask3_ ); }
	public void setUserTask3( String xx ){ UserTask3_ = xx; }

	private String UserTask4_;
	public String getUserTask4() { return( UserTask4_ ); }
	public void setUserTask4( String xx ){ UserTask4_ = xx; }

	private String UserTask5_;
	public String getUserTask5() { return( UserTask5_ ); }
	public void setUserTask5( String xx ){ UserTask5_ = xx; }

	private String Category_;
	public String getCategory() { return( Category_ ); }
	public void setCategory( String xx ){ Category_ = xx; }

	
	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request)
	{
		//String save = request.getParameter("save");
		//if( save == null ){
			//return(null);
		//}
		
		ActionErrors errors = null;

		String des = getDescription();
		if( des == null || des.equals(""))
		{
			if( errors == null )
        		errors = new ActionErrors();
			errors.add( "", new ActionMessage("errors.description"));
		}
		
		String bd = getDueDate();
		if( bd != null && !bd.equals("") )
		{
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
            try
            {
                df.parse(bd);
            }
            catch( Exception e )
            {
            	if( errors == null )
            		errors = new ActionErrors();
            	
            	errors.add( "", new ActionMessage("errors.duedate"));
                
            }
            
        }
		
	    bd = getStartDate();
		if( bd != null && !bd.equals("") )
		{
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
            try
            {
                df.parse(bd);
            }
            catch( Exception e )
            {
            	if( errors == null )
            		errors = new ActionErrors();
            	
            	errors.add( "", new ActionMessage("errors.startdate"));
                
            }
            
        }
		
		return(errors);
	}
	

};

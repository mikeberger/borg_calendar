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
package net.sf.borg.model.entity;

import java.util.Date;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;



/**
 * The Task Entity
 */
public class Task extends KeyedEntity<Task> implements CalendarEntity, java.io.Serializable {

	
	private static final long serialVersionUID = -8980203293028263282L;
	
	/** The Start date_. */
	private java.util.Date StartDate_;
	
	/**
	 * Gets the start date.
	 * 
	 * @return the start date
	 */
	public java.util.Date getStartDate() { return( StartDate_ ); }
	
	/**
	 * Sets the start date.
	 * 
	 * @param xx the new start date
	 */
	public void setStartDate( java.util.Date xx ){ StartDate_ = xx; }

	/** The C d_. */
	private java.util.Date CD_;
	
	/**
	 * Gets the cD.
	 * 
	 * @return the cD
	 */
	public java.util.Date getCD() { return( CD_ ); }
	
	/**
	 * Sets the cD.
	 * 
	 * @param xx the new cD
	 */
	public void setCD( java.util.Date xx ){ CD_ = xx; }

	/** The Due date_. */
	private java.util.Date DueDate_;
	
	/**
	 * Gets the due date.
	 * 
	 * @return the due date
	 */
	public java.util.Date getDueDate() { return( DueDate_ ); }
	
	/**
	 * Sets the due date.
	 * 
	 * @param xx the new due date
	 */
	public void setDueDate( java.util.Date xx ){ DueDate_ = xx; }

	/** The E t_. */
	private java.util.Date ET_;
	
	/**
	 * Gets the eT.
	 * 
	 * @return the eT
	 */
	public java.util.Date getET() { return( ET_ ); }
	
	/**
	 * Sets the eT.
	 * 
	 * @param xx the new eT
	 */
	public void setET( java.util.Date xx ){ ET_ = xx; }

	/** The Person assigned_. */
	private String PersonAssigned_;
	
	/**
	 * Gets the person assigned.
	 * 
	 * @return the person assigned
	 */
	public String getPersonAssigned() { return( PersonAssigned_ ); }
	
	/**
	 * Sets the person assigned.
	 * 
	 * @param xx the new person assigned
	 */
	public void setPersonAssigned( String xx ){ PersonAssigned_ = xx; }

	/** The Priority_. */
	private Integer Priority_;
	
	/**
	 * Gets the priority.
	 * 
	 * @return the priority
	 */
	public Integer getPriority() { return( Priority_ ); }
	
	/**
	 * Sets the priority.
	 * 
	 * @param xx the new priority
	 */
	public void setPriority( Integer xx ){ Priority_ = xx; }

	/** The State_. */
	private String State_;
	
	/**
	 * Gets the state.
	 * 
	 * @return the state
	 */
	public String getState() { return( State_ ); }
	
	/**
	 * Sets the state.
	 * 
	 * @param xx the new state
	 */
	public void setState( String xx ){ State_ = xx; }

	/** The Type_. */
	private String Type_;
	
	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public String getType() { return( Type_ ); }
	
	/**
	 * Sets the type.
	 * 
	 * @param xx the new type
	 */
	public void setType( String xx ){ Type_ = xx; }

	/** The Description_. */
	private String Description_;
	
	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() { return( Description_ ); }
	
	/**
	 * Sets the description.
	 * 
	 * @param xx the new description
	 */
	public void setDescription( String xx ){ Description_ = xx; }

	/** The Resolution_. */
	private String Resolution_;
	
	/**
	 * Gets the resolution.
	 * 
	 * @return the resolution
	 */
	public String getResolution() { return( Resolution_ ); }
	
	/**
	 * Sets the resolution.
	 * 
	 * @param xx the new resolution
	 */
	public void setResolution( String xx ){ Resolution_ = xx; }

	/** The Todo list_. */
	private String TodoList_;
	
	/**
	 * Gets the todo list.
	 * 
	 * @return the todo list
	 */
	public String getTodoList() { return( TodoList_ ); }
	
	/**
	 * Sets the todo list.
	 * 
	 * @param xx the new todo list
	 */
	public void setTodoList( String xx ){ TodoList_ = xx; }

	/** The User task1_. */
	private String UserTask1_;
	
	/**
	 * Gets the user task1.
	 * 
	 * @return the user task1
	 */
	public String getUserTask1() { return( UserTask1_ ); }
	
	/**
	 * Sets the user task1.
	 * 
	 * @param xx the new user task1
	 */
	public void setUserTask1( String xx ){ UserTask1_ = xx; }

	/** The User task2_. */
	private String UserTask2_;
	
	/**
	 * Gets the user task2.
	 * 
	 * @return the user task2
	 */
	public String getUserTask2() { return( UserTask2_ ); }
	
	/**
	 * Sets the user task2.
	 * 
	 * @param xx the new user task2
	 */
	public void setUserTask2( String xx ){ UserTask2_ = xx; }

	/** The User task3_. */
	private String UserTask3_;
	
	/**
	 * Gets the user task3.
	 * 
	 * @return the user task3
	 */
	public String getUserTask3() { return( UserTask3_ ); }
	
	/**
	 * Sets the user task3.
	 * 
	 * @param xx the new user task3
	 */
	public void setUserTask3( String xx ){ UserTask3_ = xx; }

	/** The User task4_. */
	private String UserTask4_;
	
	/**
	 * Gets the user task4.
	 * 
	 * @return the user task4
	 */
	public String getUserTask4() { return( UserTask4_ ); }
	
	/**
	 * Sets the user task4.
	 * 
	 * @param xx the new user task4
	 */
	public void setUserTask4( String xx ){ UserTask4_ = xx; }

	/** The User task5_. */
	private String UserTask5_;
	
	/**
	 * Gets the user task5.
	 * 
	 * @return the user task5
	 */
	public String getUserTask5() { return( UserTask5_ ); }
	
	/**
	 * Sets the user task5.
	 * 
	 * @param xx the new user task5
	 */
	public void setUserTask5( String xx ){ UserTask5_ = xx; }

	/** The Category_. */
	private String Category_;
	
	/**
	 * Gets the category.
	 * 
	 * @return the category
	 */
	public String getCategory() { return( Category_ ); }
	
	/**
	 * Sets the category.
	 * 
	 * @param xx the new category
	 */
	public void setCategory( String xx ){ Category_ = xx; }

	/** The Project_. */
	private Integer Project_;
	
	/**
	 * Gets the project.
	 * 
	 * @return the project
	 */
	public Integer getProject() { return( Project_ ); }
	
	/**
	 * Sets the project.
	 * 
	 * @param xx the new project
	 */
	public void setProject( Integer xx ){ Project_ = xx; }
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getColor()
	 */
	public String getColor()
	{
		return "navy";
	}
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getDuration()
	 */
	public Integer getDuration()
	{
		return new Integer(0);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getDate()
	 */
	public Date getDate(){ return getDueDate(); }
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getTodo()
	 */
	public boolean getTodo(){ return true; }
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getNextTodo()
	 */
	public Date getNextTodo(){ return null; }
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getText()
	 */
	public String getText(){
		 String show_abb = Prefs.getPref(PrefName.TASK_SHOW_ABBREV);
		 String abb = "";
         if (show_abb.equals("true"))
             abb = "BT" + getKey() + " ";
         String de = abb + getDescription();
         String tx = de.replace('\n', ' ');

         return tx;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.KeyedEntity#clone()
	 */
	protected Task clone() {
		Task dst = new Task();
		dst.setKey( getKey());
		dst.setStartDate( getStartDate() );
		dst.setCD( getCD() );
		dst.setDueDate( getDueDate() );
		dst.setET( getET() );
		dst.setPersonAssigned( getPersonAssigned() );
		dst.setPriority( getPriority() );
		dst.setState( getState() );
		dst.setType( getType() );
		dst.setDescription( getDescription() );
		dst.setResolution( getResolution() );
		dst.setTodoList( getTodoList() );
		dst.setUserTask1( getUserTask1() );
		dst.setUserTask2( getUserTask2() );
		dst.setUserTask3( getUserTask3() );
		dst.setUserTask4( getUserTask4() );
		dst.setUserTask5( getUserTask5() );
		dst.setCategory( getCategory() );
		dst.setProject( getProject() );
		return(dst);
	}
}

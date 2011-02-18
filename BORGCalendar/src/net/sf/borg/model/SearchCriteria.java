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

 Copyright 2003-2010 by Mike Berger
 */
package net.sf.borg.model;

import java.util.Date;

/**
 * common search criteria for searching various models.
 */
public class SearchCriteria {

	/** The case sensitive. */
	private boolean caseSensitive = false;

	// whole word flag
	private boolean wholeWord = false;

	/** The category. */
	private String category = "";

	/** The end date. */
	private Date endDate = null;

	/** The has links. */
	private boolean hasLinks = false;

	/** The holiday. */
	private boolean holiday = false;

	/** The repeating. */
	private boolean repeating = false;

	/** The search string. */
	private String searchString = "";

	/** The start date. */
	private Date startDate = null;

	/** The todo. */
	private boolean todo = false;

	/** The vacation. */
	private boolean vacation = false;

	/**
	 * Gets the category.
	 * 
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Gets the end date.
	 * 
	 * @return the end date
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * Gets the search string.
	 * 
	 * @return the search string
	 */
	public String getSearchString() {
		return searchString;
	}

	/**
	 * Gets the start date.
	 * 
	 * @return the start date
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * Checks for links.
	 * 
	 * @return true, if successful
	 */
	public boolean hasLinks() {
		return hasLinks;
	}

	/**
	 * Checks if is case sensitive.
	 * 
	 * @return true, if is case sensitive
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	/**
	 * Checks if is holiday.
	 * 
	 * @return true, if is holiday
	 */
	public boolean isHoliday() {
		return holiday;
	}

	/**
	 * Checks if is repeating.
	 * 
	 * @return true, if is repeating
	 */
	public boolean isRepeating() {
		return repeating;
	}

	/**
	 * Checks if is todo.
	 * 
	 * @return true, if is todo
	 */
	public boolean isTodo() {
		return todo;
	}

	/**
	 * Checks if is vacation.
	 * 
	 * @return true, if is vacation
	 */
	public boolean isVacation() {
		return vacation;
	}

	/**
	 * Sets the case sensitive.
	 * 
	 * @param caseSensitive
	 *            the new case sensitive
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	/**
	 * Sets the category.
	 * 
	 * @param category
	 *            the new category
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * Sets the end date.
	 * 
	 * @param endDate
	 *            the new end date
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * Sets the checks for links.
	 * 
	 * @param hasLinks
	 *            the new checks for links
	 */
	public void setHasLinks(boolean hasLinks) {
		this.hasLinks = hasLinks;
	}

	/**
	 * Sets the holiday.
	 * 
	 * @param holiday
	 *            the new holiday
	 */
	public void setHoliday(boolean holiday) {
		this.holiday = holiday;
	}

	/**
	 * Sets the repeating.
	 * 
	 * @param repeating
	 *            the new repeating
	 */
	public void setRepeating(boolean repeating) {
		this.repeating = repeating;
	}

	/**
	 * Sets the search string.
	 * 
	 * @param searchString
	 *            the new search string
	 */
	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	/**
	 * Sets the start date.
	 * 
	 * @param startDate
	 *            the new start date
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * Sets the todo.
	 * 
	 * @param todo
	 *            the new todo
	 */
	public void setTodo(boolean todo) {
		this.todo = todo;
	}

	/**
	 * Sets the vacation.
	 * 
	 * @param vacation
	 *            the new vacation
	 */
	public void setVacation(boolean vacation) {
		this.vacation = vacation;
	}

	/**
	 * get the whole word search flag
	 * 
	 */
	public boolean isWholeWord() {
		return wholeWord;
	}

	/**
	 * set the whole word search flag
	 * 
	 */
	public void setWholeWord(boolean wholeWord) {
		this.wholeWord = wholeWord;
	}

	/**
	 * do a text search on String s using any flags in the criteria (i.e. whole
	 * word and case sensitive)
	 * 
	 * @param s
	 *            - source String
	 * @return - true if there is a match
	 */
	public boolean search(String s) {
		
		String searchString = this.isCaseSensitive() ? this.getSearchString() : this.getSearchString().toLowerCase();
		String source = this.isCaseSensitive() ? s : s.toLowerCase();

		if (this.isWholeWord())
		{
			return source.matches(".*?\\b" + searchString + "\\b.*?");
		}
		
		return source.contains(searchString);


	}

}

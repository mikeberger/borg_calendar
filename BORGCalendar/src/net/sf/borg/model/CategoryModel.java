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

import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import net.sf.borg.common.Resource;

/**
 * The Class CategoryModel manages Categories. Categories are not entities, they are plain text strings.
 * Categories do not exist outside of other Entities. This class maintains a cache of category information
 * in memory but does not persist it. It recreates this information from the category-aware models as needed.
 */
public class CategoryModel extends Model {

	/**
	 * interface implemented by Models whose entities contain categories
	 */
	static interface CategorySource {
		
		/**
		 * Gets the list of all categories from all entities in the source model
		 * 
		 * @return the categories
		 */
		public Collection<String> getCategories();
	}

	/** The singleton */
	static private CategoryModel self_ = new CategoryModel();

	/** a non-null value to represent the lack of a category */
	static public final String UNCATEGORIZED = Resource
			.getResourceString("uncategorized");

	/**
	 * Gets the singleton reference.
	 * 
	 * @return the reference
	 */
	public static CategoryModel getReference() {
		return (self_);
	}

	/** The collection of all categories_. */
	private Collection<String> categories_ = new TreeSet<String>();

	/** The categories that are being shown (i.e. that are not being hidden) */
	private Collection<String> shownCategories_ = new TreeSet<String>();

	/** The set of category source models */
	Collection<CategorySource> sources = new ArrayList<CategorySource>();

	/**
	 * Add all categories from a collection to the cache
	 * 
	 * @param cats the categories
	 */
	private void addAll(Collection<String> cats) {
		categories_.addAll(cats);
		shownCategories_.addAll(cats);
	}

	/**
	 * Add a category to the cache.
	 * 
	 * @param cat the categories
	 */
	public void addCategory(String cat) {
		categories_.add(cat);
	}

	/**
	 * Add a category source.
	 * 
	 * @param s the source
	 */
	public void addSource(CategorySource s) {
		sources.add(s);
		addAll(s.getCategories());
	}

	/**
	 * Get all categories.
	 * 
	 * @return the categories
	 * 
	 * @throws Exception the exception
	 */
	public Collection<String> getCategories() throws Exception {
		ArrayList<String> cats = new ArrayList<String>();
		cats.addAll(categories_);
		return (cats);
	}

	/**
	 * Get the shown categories.
	 * 
	 * @return the shown categories
	 */
	public Collection<String> getShownCategories() {
		ArrayList<String> cats = new ArrayList<String>();
		cats.addAll(shownCategories_);
		return (cats);
	}

	/**
	 * Checks if a category is being shown.
	 * 
	 * @param cat the cat
	 * 
	 * @return true, if is shown
	 */
	public boolean isShown(String cat) {
		return (shownCategories_.contains((cat == null || cat.equals("")) ? CategoryModel.UNCATEGORIZED : cat)) ;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.Model#remove()
	 */
	@Override
	public void remove() {
	  // empty
	}

	/**
	 * Sets the set of shown categories.
	 * 
	 * @param cats the shown categories
	 */
	public void setShownCategories(Collection<String> cats) {
		shownCategories_ = cats;
		refreshListeners();
	}

	/**
	 * Show all categories.
	 */
	public void showAll() {
		shownCategories_.clear();
		shownCategories_.addAll(categories_);
		refreshListeners();
	}

	/**
	 * Show a particular category.
	 * 
	 * @param cat the category to show
	 */
	public void showCategory(String cat) {
		shownCategories_.add(cat);
	}

	/**
	 * Sync categories with the sources (clears the cache and re-reads the list of categories).
	 * 
	 * @throws Exception the exception
	 */
	public void syncCategories() throws Exception {
		categories_.clear();
		for( CategorySource s : sources )
			categories_.addAll(s.getCategories());
		
	}

	@Override
	public void export(Writer fw) throws Exception {
		// nothing to export
	}

	@Override
	public void importXml(InputStream is) throws Exception {
		// nothing to import
	}
	
	@Override
	public String getExportName() {
		return null;
	}

	@Override
	public String getInfo() throws Exception {
		return Resource.getResourceString("Categories") + ":" + getCategories().size();
	}

}

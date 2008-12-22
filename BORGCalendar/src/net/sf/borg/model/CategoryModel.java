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

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import net.sf.borg.common.Resource;

public class CategoryModel extends Model {

	static interface CategorySource {
		public Collection<String> getCategories();
	}

	Collection<CategorySource> sources = new ArrayList<CategorySource>();

	static private CategoryModel self_ = new CategoryModel();

	static public final String UNCATEGORIZED = Resource
			.getResourceString("uncategorized");

	public void addSource(CategorySource s) {
		sources.add(s);
		addAll(s.getCategories());
	}

	public static CategoryModel getReference() {
		return (self_);
	}

	// categories in DB
	private Collection<String> categories_ = new TreeSet<String>();

	// categories being displayed
	private Collection<String> shownCategories_ = new TreeSet<String>();

	public void addAll(Collection<String> cats) {
		categories_.addAll(cats);
		shownCategories_.addAll(cats);
	}

	public void addCategory(String cat) {
		categories_.add(cat);
	}

	public Collection<String> getCategories() throws Exception {
		ArrayList<String> cats = new ArrayList<String>();
		cats.addAll(categories_);
		return (cats);
	}

	public Collection<String> getShownCategories() {
		ArrayList<String> cats = new ArrayList<String>();
		cats.addAll(shownCategories_);
		return (cats);
	}

	public boolean isShown(String cat) {
		if (cat == null || cat.equals(""))
			cat = CategoryModel.UNCATEGORIZED;
		if (shownCategories_.contains(cat))
			return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.Model#remove()
	 */
	public void remove() {

	}

	public void setShownCategories(Collection<String> cats) {
		shownCategories_ = cats;
		refreshListeners();
	}

	public void showAll() {
		shownCategories_.clear();
		shownCategories_.addAll(categories_);
		refreshListeners();
	}

	public void showCategory(String cat) {
		shownCategories_.add(cat);
	}

	public void syncCategories() throws Exception {
		categories_.clear();
		for( CategorySource s : sources )
			categories_.addAll(s.getCategories());
		
	}

}

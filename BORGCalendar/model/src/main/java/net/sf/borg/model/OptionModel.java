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

import java.io.InputStream;
import java.io.Writer;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.borg.common.Resource;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.db.OptionDB;
import net.sf.borg.model.entity.Option;

/**
 * The Option Model manages the Borg Options
 */
public class OptionModel extends Model implements Searchable<Option> {

	/**
	 * class XmlContainer is solely for JAXB XML export/import
	 */
	@XmlRootElement(name = "OPTIONS")
	private static class XmlContainer {
		public Collection<Option> Option;
	}

	/** The db */
	private OptionDB db_; // the database

	/** The singleton */
	static private OptionModel self_ = new OptionModel();

	/**
	 * Gets the singleton.
	 * 
	 * @return the singleton
	 */
	public static OptionModel getReference() {
		return (self_);
	}

	/**
	 * Gets all options.
	 * 
	 * @return all options
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Collection<Option> getOptions() throws Exception {
		return db_.getOptions();
	}

	
	/**
	 * Instantiates a new option model.
	 */
	private OptionModel() {
		db_ = DBHelper.getFactory().createOptionDB();
	}

	
	/**
	 * set an option.
	 * 
	 * @param option
	 *            the option
	 * @throws Exception 
	 */
	public void setOption(Option option) throws Exception {
		db_.setOption(option);
	}

	
	/**
	 * Gets an option by name.
	 * 
	 * @param name
	 *            the option name
	 * 
	 * @return the option
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public String getOption(String name) throws Exception {
		return db_.getOption(name);
	}

	/**
	 * Export to XML
	 * 
	 * @param fw
	 *            the writer to write XML to
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public void export(Writer fw) throws Exception {

		JAXBContext jc = JAXBContext.newInstance(XmlContainer.class);
		Marshaller m = jc.createMarshaller();
		XmlContainer container = new XmlContainer();
		container.Option = getOptions();
		m.marshal(container, fw);

	}

	/**
	 * Import xml.
	 * 
	 * @param is
	 *            the input stream containing the XML
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public void importXml(InputStream is) throws Exception {

		JAXBContext jc = JAXBContext.newInstance(XmlContainer.class);
		Unmarshaller u = jc.createUnmarshaller();

		XmlContainer container = (XmlContainer) u.unmarshal(is);

		if (container.Option == null)
			return;

		for (Option option : container.Option) {
			setOption(option);
		}

		refresh();
	}
	
	/**
	 * import options from a collection. This code supports the old format import where the TaskModel reads
	 * in the Options XML
	 * @param options
	 * @throws Exception 
	 */
	public void importOptions(Collection<Option> options) throws Exception
	{
		for (Option option : options) {
			setOption(option);
		}

		refresh();
	}

	/**
	 * Refresh listeners
	 */
	public void refresh() {
		refreshListeners();
	}

	
	@Override
	public String getExportName() {
		return "OPTIONS";
	}

	@Override
	public String getInfo() throws Exception {
		return Resource.getResourceString("Options") + ": "
				+ getOptions().size();
	}

	@Override
	public Collection<Option> search(SearchCriteria criteria) {
		return null; 
	}
}

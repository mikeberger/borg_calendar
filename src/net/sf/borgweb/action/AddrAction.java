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
package net.sf.borgweb.action;

import java.io.IOException;
import java.text.DateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.borg.model.Address;
import net.sf.borg.model.AddressModel;
import net.sf.borgweb.form.AddressForm;
import net.sf.borgweb.util.EH;
import net.sf.borgweb.util.GetModel;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class AddrAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		HttpSession sess = request.getSession();
		// System.out.println(mapping.getPath());
		String cmd = mapping.getPath();
		try {

			if (cmd.endsWith("/editAddr")) {
				// determine if this is add, edit, delete
				String key = request.getParameter("key");
				if (key != null) {

					// load data from db into form
					AddressForm af = (AddressForm) form;
					AddressModel model = GetModel.getAddressModel(sess,request.getUserPrincipal().getName());
					af.setKey(key);
					int k = Integer.parseInt(key);
					Address addr = model.getAddress(k);
					af.setFirstName(addr.getFirstName());
					af.setLastName(addr.getLastName());
					af.setEmail(addr.getEmail());
					af.setNickname(addr.getNickname());
					af.setScreenName(addr.getScreenName());
					af.setFax(addr.getFax());
					af.setPager(addr.getPager());
					af.setWorkPhone(addr.getWorkPhone());
					af.setHomePhone(addr.getHomePhone());
					af.setWebPage(addr.getWebPage());
					af.setCompany(addr.getCompany());
					java.util.Date bd = addr.getBirthday();
					if (bd != null) {
						DateFormat df = DateFormat
								.getDateInstance(DateFormat.SHORT);
						af.setBirthday(df.format(bd));
					}
					af.setStreetAddress(addr.getStreetAddress());
					af.setCity(addr.getCity());
					af.setState(addr.getState());
					af.setCountry(addr.getCountry());
					af.setZip(addr.getZip());
					af.setWorkStreetAddress(addr.getWorkStreetAddress());
					af.setWorkCity(addr.getWorkCity());
					af.setWorkState(addr.getWorkState());
					af.setWorkCountry(addr.getWorkCountry());
					af.setWorkZip(addr.getWorkZip());
					af.setNotes(addr.getNotes());
					return (mapping.findForward("success"));

				}
			} else if (cmd.endsWith("/saveAddr")) {
				// determine if this is add, edit, delete
				String key = request.getParameter("save");
				if (key != null) {
					// get model from session or create
					AddressModel am = GetModel.getAddressModel(sess,request.getUserPrincipal().getName());

					Address addr = am.newAddress();
					AddressForm af = (AddressForm) form;
					addr.setFirstName(af.getFirstName());
					addr.setLastName(af.getLastName());
					addr.setEmail(af.getEmail());
					addr.setNickname(af.getNickname());
					addr.setScreenName(af.getScreenName());
					addr.setFax(af.getFax());
					addr.setPager(af.getPager());
					addr.setWorkPhone(af.getWorkPhone());
					addr.setHomePhone(af.getHomePhone());
					addr.setWebPage(af.getWebPage());
					addr.setCompany(af.getCompany());
					String bd = af.getBirthday();
					if (bd != null && !bd.equals("")) {
						DateFormat df = DateFormat
								.getDateInstance(DateFormat.SHORT);
						addr.setBirthday(df.parse(bd));
					}
					addr.setStreetAddress(af.getStreetAddress());
					addr.setCity(af.getCity());
					addr.setState(af.getState());
					addr.setCountry(af.getCountry());
					addr.setZip(af.getZip());
					addr.setWorkStreetAddress(af.getWorkStreetAddress());
					addr.setWorkCity(af.getWorkCity());
					addr.setWorkState(af.getWorkState());
					addr.setWorkCountry(af.getWorkCountry());
					addr.setWorkZip(af.getWorkZip());
					addr.setNotes(af.getNotes());

					// check if this is an edit or add
					key = af.getKey();
					addr.setKey(Integer.parseInt(key));

					am.saveAddress(addr);
					return (mapping.findForward("success"));

				}
			} else if (cmd.endsWith("/delAddr")) {
				// determine if this is add, edit, delete
				String key = request.getParameter("key");
				if (key != null) {

					// load data from db into form
					AddressModel model = GetModel.getAddressModel(sess,request.getUserPrincipal().getName());
					int k = Integer.parseInt(key);
					Address addr = new Address();
					addr.setKey(k);
					model.delete(addr, false);
					return (mapping.findForward("success"));

				}
			} else if (cmd.endsWith("/newAddr")) {
				// determine if this is add, edit, delete

				AddressForm af = (AddressForm) form;
				af.setKey("-1");
				return (mapping.findForward("success"));

			}

		} catch (Exception e) {
			throw new ServletException(EH.stackTrace(e));
		}

		return (mapping.findForward("failure"));

	}
}

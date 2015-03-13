package net.sf.borg.ui.popup;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.entity.Address;

/**
 * holds reminders based on birthdays in the address book
 *
 */
public class BirthdayReminderInstance extends ReminderInstance {

	private Address addr;
	
	/**
	 * constructor
	 * @param addr - the address
	 * @param instanceTime - the birthday instance time
	 */
	public BirthdayReminderInstance(Address addr, Date instanceTime) {
		this.addr = addr;
		this.setInstanceTime(instanceTime);
	}

	@Override
	public void do_todo(boolean delete) {
		// do nothing
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BirthdayReminderInstance other = (BirthdayReminderInstance) obj;
		if (addr == null) {
			if (other.addr != null)
				return false;
		} else if (addr.getKey() != other.addr.getKey()) {
			return false;
		}

		if (getInstanceTime() == null) {
			if (other.getInstanceTime() != null)
				return false;
		} else if (!getInstanceTime().equals(other.getInstanceTime()))
			return false;
		return true;
	}

	@Override
	public int getCurrentReminder() {
		// birthdays do not use the set reminder times
		return -1;
	}

	@Override
	public String getText() {
		GregorianCalendar inst = new GregorianCalendar();
		inst.setTime(getInstanceTime());
		
		Date bd = addr.getBirthday();
		GregorianCalendar g = new GregorianCalendar();
		g.setTime(bd);
		int bdyear = g.get(Calendar.YEAR);
		int yrs = inst.get(Calendar.YEAR) - bdyear;

		String tx = Resource.getResourceString("Birthday") + ": "
				+ addr.getFirstName() + " " + addr.getLastName() + "("
				+ yrs + ")";
		return tx;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addr == null) ? 0 : addr.getKey());
		result = prime * result
				+ ((getInstanceTime() == null) ? 0 : getInstanceTime().hashCode());
		return result;
	}

	@Override
	public String calculateToGoMessage() {
		return Resource.getResourceString("Birthday");
	}

	@Override
	public boolean isNote() {
		return true;
	}

	@Override
	public boolean isTodo() {
		// treat as todo for determining when to display
		return true;
	}

	@Override
	public boolean reloadAndCheckForChanges() {
		try {
			Address orig = addr;
			addr = AddressModel.getReference().getAddress(addr.getKey());
			if (addr == null) {
				return true;
			}
			
			if (addr.getBirthday() == null)
				return true;

			if (!addr.getBirthday()
					.equals(orig.getBirthday())) {
				// date changed - delete. new instance will be added on
				// periodic update
				return true;
			}
			
		} catch (Exception e) {

			// cannot be read, must have been deleted
			// this is an expected case when items are deleted
			addr = null;
			return true;
		}
		
		return false;
	}

	@Override
	public boolean shouldBeShown() {
		if( addr == null || addr.getBirthday() == null )
			return false;

		// determine how far away the task is
		long minutesToGo = getInstanceTime().getTime() / (1000 * 60)
				- new Date().getTime() / (1000 * 60);

		if( minutesToGo < 0) return false;
		
		int bd_days = Prefs.getIntPref(PrefName.BIRTHDAYREMINDERDAYS);
		bd_days += 1; // 0 means show on the current day, 1 means 1 day before - so need to increment bd_days
		
		return (minutesToGo < bd_days*24*60);
	}

}

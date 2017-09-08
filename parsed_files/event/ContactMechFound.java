package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContactMechFound implements Event{

	private List<ContactMech> contactMechs;

	public ContactMechFound(List<ContactMech> contactMechs) {
		this.setContactMechs(contactMechs);
	}

	public List<ContactMech> getContactMechs()	{
		return contactMechs;
	}

	public void setContactMechs(List<ContactMech> contactMechs)	{
		this.contactMechs = contactMechs;
	}
}

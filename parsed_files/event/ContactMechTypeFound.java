package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContactMechTypeFound implements Event{

	private List<ContactMechType> contactMechTypes;

	public ContactMechTypeFound(List<ContactMechType> contactMechTypes) {
		this.setContactMechTypes(contactMechTypes);
	}

	public List<ContactMechType> getContactMechTypes()	{
		return contactMechTypes;
	}

	public void setContactMechTypes(List<ContactMechType> contactMechTypes)	{
		this.contactMechTypes = contactMechTypes;
	}
}

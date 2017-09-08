package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContactMechAttributeFound implements Event{

	private List<ContactMechAttribute> contactMechAttributes;

	public ContactMechAttributeFound(List<ContactMechAttribute> contactMechAttributes) {
		this.setContactMechAttributes(contactMechAttributes);
	}

	public List<ContactMechAttribute> getContactMechAttributes()	{
		return contactMechAttributes;
	}

	public void setContactMechAttributes(List<ContactMechAttribute> contactMechAttributes)	{
		this.contactMechAttributes = contactMechAttributes;
	}
}

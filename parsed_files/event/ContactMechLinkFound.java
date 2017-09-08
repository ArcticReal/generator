package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContactMechLinkFound implements Event{

	private List<ContactMechLink> contactMechLinks;

	public ContactMechLinkFound(List<ContactMechLink> contactMechLinks) {
		this.setContactMechLinks(contactMechLinks);
	}

	public List<ContactMechLink> getContactMechLinks()	{
		return contactMechLinks;
	}

	public void setContactMechLinks(List<ContactMechLink> contactMechLinks)	{
		this.contactMechLinks = contactMechLinks;
	}
}

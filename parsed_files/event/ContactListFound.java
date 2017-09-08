package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContactListFound implements Event{

	private List<ContactList> contactLists;

	public ContactListFound(List<ContactList> contactLists) {
		this.setContactLists(contactLists);
	}

	public List<ContactList> getContactLists()	{
		return contactLists;
	}

	public void setContactLists(List<ContactList> contactLists)	{
		this.contactLists = contactLists;
	}
}

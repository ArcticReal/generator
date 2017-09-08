package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContactListTypeFound implements Event{

	private List<ContactListType> contactListTypes;

	public ContactListTypeFound(List<ContactListType> contactListTypes) {
		this.setContactListTypes(contactListTypes);
	}

	public List<ContactListType> getContactListTypes()	{
		return contactListTypes;
	}

	public void setContactListTypes(List<ContactListType> contactListTypes)	{
		this.contactListTypes = contactListTypes;
	}
}

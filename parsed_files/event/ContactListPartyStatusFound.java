package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContactListPartyStatusFound implements Event{

	private List<ContactListPartyStatus> contactListPartyStatuss;

	public ContactListPartyStatusFound(List<ContactListPartyStatus> contactListPartyStatuss) {
		this.setContactListPartyStatuss(contactListPartyStatuss);
	}

	public List<ContactListPartyStatus> getContactListPartyStatuss()	{
		return contactListPartyStatuss;
	}

	public void setContactListPartyStatuss(List<ContactListPartyStatus> contactListPartyStatuss)	{
		this.contactListPartyStatuss = contactListPartyStatuss;
	}
}

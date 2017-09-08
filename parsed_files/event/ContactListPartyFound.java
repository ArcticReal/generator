package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContactListPartyFound implements Event{

	private List<ContactListParty> contactListPartys;

	public ContactListPartyFound(List<ContactListParty> contactListPartys) {
		this.setContactListPartys(contactListPartys);
	}

	public List<ContactListParty> getContactListPartys()	{
		return contactListPartys;
	}

	public void setContactListPartys(List<ContactListParty> contactListPartys)	{
		this.contactListPartys = contactListPartys;
	}
}

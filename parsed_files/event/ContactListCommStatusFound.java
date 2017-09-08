package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContactListCommStatusFound implements Event{

	private List<ContactListCommStatus> contactListCommStatuss;

	public ContactListCommStatusFound(List<ContactListCommStatus> contactListCommStatuss) {
		this.setContactListCommStatuss(contactListCommStatuss);
	}

	public List<ContactListCommStatus> getContactListCommStatuss()	{
		return contactListCommStatuss;
	}

	public void setContactListCommStatuss(List<ContactListCommStatus> contactListCommStatuss)	{
		this.contactListCommStatuss = contactListCommStatuss;
	}
}

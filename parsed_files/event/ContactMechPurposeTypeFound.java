package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContactMechPurposeTypeFound implements Event{

	private List<ContactMechPurposeType> contactMechPurposeTypes;

	public ContactMechPurposeTypeFound(List<ContactMechPurposeType> contactMechPurposeTypes) {
		this.setContactMechPurposeTypes(contactMechPurposeTypes);
	}

	public List<ContactMechPurposeType> getContactMechPurposeTypes()	{
		return contactMechPurposeTypes;
	}

	public void setContactMechPurposeTypes(List<ContactMechPurposeType> contactMechPurposeTypes)	{
		this.contactMechPurposeTypes = contactMechPurposeTypes;
	}
}

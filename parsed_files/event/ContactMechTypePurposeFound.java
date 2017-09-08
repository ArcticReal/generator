package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContactMechTypePurposeFound implements Event{

	private List<ContactMechTypePurpose> contactMechTypePurposes;

	public ContactMechTypePurposeFound(List<ContactMechTypePurpose> contactMechTypePurposes) {
		this.setContactMechTypePurposes(contactMechTypePurposes);
	}

	public List<ContactMechTypePurpose> getContactMechTypePurposes()	{
		return contactMechTypePurposes;
	}

	public void setContactMechTypePurposes(List<ContactMechTypePurpose> contactMechTypePurposes)	{
		this.contactMechTypePurposes = contactMechTypePurposes;
	}
}

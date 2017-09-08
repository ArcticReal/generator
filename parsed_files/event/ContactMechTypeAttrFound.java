package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContactMechTypeAttrFound implements Event{

	private List<ContactMechTypeAttr> contactMechTypeAttrs;

	public ContactMechTypeAttrFound(List<ContactMechTypeAttr> contactMechTypeAttrs) {
		this.setContactMechTypeAttrs(contactMechTypeAttrs);
	}

	public List<ContactMechTypeAttr> getContactMechTypeAttrs()	{
		return contactMechTypeAttrs;
	}

	public void setContactMechTypeAttrs(List<ContactMechTypeAttr> contactMechTypeAttrs)	{
		this.contactMechTypeAttrs = contactMechTypeAttrs;
	}
}

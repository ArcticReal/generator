package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentRoleFound implements Event{

	private List<ContentRole> contentRoles;

	public ContentRoleFound(List<ContentRole> contentRoles) {
		this.setContentRoles(contentRoles);
	}

	public List<ContentRole> getContentRoles()	{
		return contentRoles;
	}

	public void setContentRoles(List<ContentRole> contentRoles)	{
		this.contentRoles = contentRoles;
	}
}

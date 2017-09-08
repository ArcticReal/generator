package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentAssocTypeFound implements Event{

	private List<ContentAssocType> contentAssocTypes;

	public ContentAssocTypeFound(List<ContentAssocType> contentAssocTypes) {
		this.setContentAssocTypes(contentAssocTypes);
	}

	public List<ContentAssocType> getContentAssocTypes()	{
		return contentAssocTypes;
	}

	public void setContentAssocTypes(List<ContentAssocType> contentAssocTypes)	{
		this.contentAssocTypes = contentAssocTypes;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentTypeFound implements Event{

	private List<ContentType> contentTypes;

	public ContentTypeFound(List<ContentType> contentTypes) {
		this.setContentTypes(contentTypes);
	}

	public List<ContentType> getContentTypes()	{
		return contentTypes;
	}

	public void setContentTypes(List<ContentType> contentTypes)	{
		this.contentTypes = contentTypes;
	}
}

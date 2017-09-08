package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentAttributeFound implements Event{

	private List<ContentAttribute> contentAttributes;

	public ContentAttributeFound(List<ContentAttribute> contentAttributes) {
		this.setContentAttributes(contentAttributes);
	}

	public List<ContentAttribute> getContentAttributes()	{
		return contentAttributes;
	}

	public void setContentAttributes(List<ContentAttribute> contentAttributes)	{
		this.contentAttributes = contentAttributes;
	}
}

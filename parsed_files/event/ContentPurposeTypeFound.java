package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentPurposeTypeFound implements Event{

	private List<ContentPurposeType> contentPurposeTypes;

	public ContentPurposeTypeFound(List<ContentPurposeType> contentPurposeTypes) {
		this.setContentPurposeTypes(contentPurposeTypes);
	}

	public List<ContentPurposeType> getContentPurposeTypes()	{
		return contentPurposeTypes;
	}

	public void setContentPurposeTypes(List<ContentPurposeType> contentPurposeTypes)	{
		this.contentPurposeTypes = contentPurposeTypes;
	}
}

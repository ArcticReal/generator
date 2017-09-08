package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentPurposeFound implements Event{

	private List<ContentPurpose> contentPurposes;

	public ContentPurposeFound(List<ContentPurpose> contentPurposes) {
		this.setContentPurposes(contentPurposes);
	}

	public List<ContentPurpose> getContentPurposes()	{
		return contentPurposes;
	}

	public void setContentPurposes(List<ContentPurpose> contentPurposes)	{
		this.contentPurposes = contentPurposes;
	}
}

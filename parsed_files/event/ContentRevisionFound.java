package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentRevisionFound implements Event{

	private List<ContentRevision> contentRevisions;

	public ContentRevisionFound(List<ContentRevision> contentRevisions) {
		this.setContentRevisions(contentRevisions);
	}

	public List<ContentRevision> getContentRevisions()	{
		return contentRevisions;
	}

	public void setContentRevisions(List<ContentRevision> contentRevisions)	{
		this.contentRevisions = contentRevisions;
	}
}

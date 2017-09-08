package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentOperationFound implements Event{

	private List<ContentOperation> contentOperations;

	public ContentOperationFound(List<ContentOperation> contentOperations) {
		this.setContentOperations(contentOperations);
	}

	public List<ContentOperation> getContentOperations()	{
		return contentOperations;
	}

	public void setContentOperations(List<ContentOperation> contentOperations)	{
		this.contentOperations = contentOperations;
	}
}

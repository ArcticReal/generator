package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentPurposeOperationFound implements Event{

	private List<ContentPurposeOperation> contentPurposeOperations;

	public ContentPurposeOperationFound(List<ContentPurposeOperation> contentPurposeOperations) {
		this.setContentPurposeOperations(contentPurposeOperations);
	}

	public List<ContentPurposeOperation> getContentPurposeOperations()	{
		return contentPurposeOperations;
	}

	public void setContentPurposeOperations(List<ContentPurposeOperation> contentPurposeOperations)	{
		this.contentPurposeOperations = contentPurposeOperations;
	}
}

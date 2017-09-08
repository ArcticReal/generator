package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class DocumentAttributeFound implements Event{

	private List<DocumentAttribute> documentAttributes;

	public DocumentAttributeFound(List<DocumentAttribute> documentAttributes) {
		this.setDocumentAttributes(documentAttributes);
	}

	public List<DocumentAttribute> getDocumentAttributes()	{
		return documentAttributes;
	}

	public void setDocumentAttributes(List<DocumentAttribute> documentAttributes)	{
		this.documentAttributes = documentAttributes;
	}
}

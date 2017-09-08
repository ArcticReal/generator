package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class DocumentTypeFound implements Event{

	private List<DocumentType> documentTypes;

	public DocumentTypeFound(List<DocumentType> documentTypes) {
		this.setDocumentTypes(documentTypes);
	}

	public List<DocumentType> getDocumentTypes()	{
		return documentTypes;
	}

	public void setDocumentTypes(List<DocumentType> documentTypes)	{
		this.documentTypes = documentTypes;
	}
}

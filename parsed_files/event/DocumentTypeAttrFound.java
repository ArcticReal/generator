package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class DocumentTypeAttrFound implements Event{

	private List<DocumentTypeAttr> documentTypeAttrs;

	public DocumentTypeAttrFound(List<DocumentTypeAttr> documentTypeAttrs) {
		this.setDocumentTypeAttrs(documentTypeAttrs);
	}

	public List<DocumentTypeAttr> getDocumentTypeAttrs()	{
		return documentTypeAttrs;
	}

	public void setDocumentTypeAttrs(List<DocumentTypeAttr> documentTypeAttrs)	{
		this.documentTypeAttrs = documentTypeAttrs;
	}
}

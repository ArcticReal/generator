package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentTypeAttrFound implements Event{

	private List<ContentTypeAttr> contentTypeAttrs;

	public ContentTypeAttrFound(List<ContentTypeAttr> contentTypeAttrs) {
		this.setContentTypeAttrs(contentTypeAttrs);
	}

	public List<ContentTypeAttr> getContentTypeAttrs()	{
		return contentTypeAttrs;
	}

	public void setContentTypeAttrs(List<ContentTypeAttr> contentTypeAttrs)	{
		this.contentTypeAttrs = contentTypeAttrs;
	}
}

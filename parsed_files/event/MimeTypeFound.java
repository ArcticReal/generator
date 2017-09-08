package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class MimeTypeFound implements Event{

	private List<MimeType> mimeTypes;

	public MimeTypeFound(List<MimeType> mimeTypes) {
		this.setMimeTypes(mimeTypes);
	}

	public List<MimeType> getMimeTypes()	{
		return mimeTypes;
	}

	public void setMimeTypes(List<MimeType> mimeTypes)	{
		this.mimeTypes = mimeTypes;
	}
}

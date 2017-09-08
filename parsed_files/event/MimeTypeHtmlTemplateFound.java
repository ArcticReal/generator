package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class MimeTypeHtmlTemplateFound implements Event{

	private List<MimeTypeHtmlTemplate> mimeTypeHtmlTemplates;

	public MimeTypeHtmlTemplateFound(List<MimeTypeHtmlTemplate> mimeTypeHtmlTemplates) {
		this.setMimeTypeHtmlTemplates(mimeTypeHtmlTemplates);
	}

	public List<MimeTypeHtmlTemplate> getMimeTypeHtmlTemplates()	{
		return mimeTypeHtmlTemplates;
	}

	public void setMimeTypeHtmlTemplates(List<MimeTypeHtmlTemplate> mimeTypeHtmlTemplates)	{
		this.mimeTypeHtmlTemplates = mimeTypeHtmlTemplates;
	}
}

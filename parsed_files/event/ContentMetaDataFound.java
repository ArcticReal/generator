package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentMetaDataFound implements Event{

	private List<ContentMetaData> contentMetaDatas;

	public ContentMetaDataFound(List<ContentMetaData> contentMetaDatas) {
		this.setContentMetaDatas(contentMetaDatas);
	}

	public List<ContentMetaData> getContentMetaDatas()	{
		return contentMetaDatas;
	}

	public void setContentMetaDatas(List<ContentMetaData> contentMetaDatas)	{
		this.contentMetaDatas = contentMetaDatas;
	}
}

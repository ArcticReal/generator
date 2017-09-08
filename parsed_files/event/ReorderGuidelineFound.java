package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ReorderGuidelineFound implements Event{

	private List<ReorderGuideline> reorderGuidelines;

	public ReorderGuidelineFound(List<ReorderGuideline> reorderGuidelines) {
		this.setReorderGuidelines(reorderGuidelines);
	}

	public List<ReorderGuideline> getReorderGuidelines()	{
		return reorderGuidelines;
	}

	public void setReorderGuidelines(List<ReorderGuideline> reorderGuidelines)	{
		this.reorderGuidelines = reorderGuidelines;
	}
}

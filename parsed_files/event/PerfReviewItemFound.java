package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PerfReviewItemFound implements Event{

	private List<PerfReviewItem> perfReviewItems;

	public PerfReviewItemFound(List<PerfReviewItem> perfReviewItems) {
		this.setPerfReviewItems(perfReviewItems);
	}

	public List<PerfReviewItem> getPerfReviewItems()	{
		return perfReviewItems;
	}

	public void setPerfReviewItems(List<PerfReviewItem> perfReviewItems)	{
		this.perfReviewItems = perfReviewItems;
	}
}

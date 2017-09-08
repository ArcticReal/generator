package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SegmentGroupClassificationFound implements Event{

	private List<SegmentGroupClassification> segmentGroupClassifications;

	public SegmentGroupClassificationFound(List<SegmentGroupClassification> segmentGroupClassifications) {
		this.setSegmentGroupClassifications(segmentGroupClassifications);
	}

	public List<SegmentGroupClassification> getSegmentGroupClassifications()	{
		return segmentGroupClassifications;
	}

	public void setSegmentGroupClassifications(List<SegmentGroupClassification> segmentGroupClassifications)	{
		this.segmentGroupClassifications = segmentGroupClassifications;
	}
}

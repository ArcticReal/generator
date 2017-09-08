package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SegmentGroupTypeFound implements Event{

	private List<SegmentGroupType> segmentGroupTypes;

	public SegmentGroupTypeFound(List<SegmentGroupType> segmentGroupTypes) {
		this.setSegmentGroupTypes(segmentGroupTypes);
	}

	public List<SegmentGroupType> getSegmentGroupTypes()	{
		return segmentGroupTypes;
	}

	public void setSegmentGroupTypes(List<SegmentGroupType> segmentGroupTypes)	{
		this.segmentGroupTypes = segmentGroupTypes;
	}
}

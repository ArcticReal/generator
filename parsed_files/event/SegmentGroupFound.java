package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SegmentGroupFound implements Event{

	private List<SegmentGroup> segmentGroups;

	public SegmentGroupFound(List<SegmentGroup> segmentGroups) {
		this.setSegmentGroups(segmentGroups);
	}

	public List<SegmentGroup> getSegmentGroups()	{
		return segmentGroups;
	}

	public void setSegmentGroups(List<SegmentGroup> segmentGroups)	{
		this.segmentGroups = segmentGroups;
	}
}

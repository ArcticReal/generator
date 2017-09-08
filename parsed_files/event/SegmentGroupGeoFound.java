package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SegmentGroupGeoFound implements Event{

	private List<SegmentGroupGeo> segmentGroupGeos;

	public SegmentGroupGeoFound(List<SegmentGroupGeo> segmentGroupGeos) {
		this.setSegmentGroupGeos(segmentGroupGeos);
	}

	public List<SegmentGroupGeo> getSegmentGroupGeos()	{
		return segmentGroupGeos;
	}

	public void setSegmentGroupGeos(List<SegmentGroupGeo> segmentGroupGeos)	{
		this.segmentGroupGeos = segmentGroupGeos;
	}
}

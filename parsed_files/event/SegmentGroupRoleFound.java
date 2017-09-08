package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SegmentGroupRoleFound implements Event{

	private List<SegmentGroupRole> segmentGroupRoles;

	public SegmentGroupRoleFound(List<SegmentGroupRole> segmentGroupRoles) {
		this.setSegmentGroupRoles(segmentGroupRoles);
	}

	public List<SegmentGroupRole> getSegmentGroupRoles()	{
		return segmentGroupRoles;
	}

	public void setSegmentGroupRoles(List<SegmentGroupRole> segmentGroupRoles)	{
		this.segmentGroupRoles = segmentGroupRoles;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ContentApprovalFound implements Event{

	private List<ContentApproval> contentApprovals;

	public ContentApprovalFound(List<ContentApproval> contentApprovals) {
		this.setContentApprovals(contentApprovals);
	}

	public List<ContentApproval> getContentApprovals()	{
		return contentApprovals;
	}

	public void setContentApprovals(List<ContentApproval> contentApprovals)	{
		this.contentApprovals = contentApprovals;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderRequirementCommitmentFound implements Event{

	private List<OrderRequirementCommitment> orderRequirementCommitments;

	public OrderRequirementCommitmentFound(List<OrderRequirementCommitment> orderRequirementCommitments) {
		this.setOrderRequirementCommitments(orderRequirementCommitments);
	}

	public List<OrderRequirementCommitment> getOrderRequirementCommitments()	{
		return orderRequirementCommitments;
	}

	public void setOrderRequirementCommitments(List<OrderRequirementCommitment> orderRequirementCommitments)	{
		this.orderRequirementCommitments = orderRequirementCommitments;
	}
}

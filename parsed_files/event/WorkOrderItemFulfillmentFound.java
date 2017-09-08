package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkOrderItemFulfillmentFound implements Event{

	private List<WorkOrderItemFulfillment> workOrderItemFulfillments;

	public WorkOrderItemFulfillmentFound(List<WorkOrderItemFulfillment> workOrderItemFulfillments) {
		this.setWorkOrderItemFulfillments(workOrderItemFulfillments);
	}

	public List<WorkOrderItemFulfillment> getWorkOrderItemFulfillments()	{
		return workOrderItemFulfillments;
	}

	public void setWorkOrderItemFulfillments(List<WorkOrderItemFulfillment> workOrderItemFulfillments)	{
		this.workOrderItemFulfillments = workOrderItemFulfillments;
	}
}

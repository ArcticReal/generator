package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentBudgetAllocationFound implements Event{

	private List<PaymentBudgetAllocation> paymentBudgetAllocations;

	public PaymentBudgetAllocationFound(List<PaymentBudgetAllocation> paymentBudgetAllocations) {
		this.setPaymentBudgetAllocations(paymentBudgetAllocations);
	}

	public List<PaymentBudgetAllocation> getPaymentBudgetAllocations()	{
		return paymentBudgetAllocations;
	}

	public void setPaymentBudgetAllocations(List<PaymentBudgetAllocation> paymentBudgetAllocations)	{
		this.paymentBudgetAllocations = paymentBudgetAllocations;
	}
}

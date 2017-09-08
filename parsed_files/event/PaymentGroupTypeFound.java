package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGroupTypeFound implements Event{

	private List<PaymentGroupType> paymentGroupTypes;

	public PaymentGroupTypeFound(List<PaymentGroupType> paymentGroupTypes) {
		this.setPaymentGroupTypes(paymentGroupTypes);
	}

	public List<PaymentGroupType> getPaymentGroupTypes()	{
		return paymentGroupTypes;
	}

	public void setPaymentGroupTypes(List<PaymentGroupType> paymentGroupTypes)	{
		this.paymentGroupTypes = paymentGroupTypes;
	}
}

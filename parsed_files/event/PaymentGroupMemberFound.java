package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGroupMemberFound implements Event{

	private List<PaymentGroupMember> paymentGroupMembers;

	public PaymentGroupMemberFound(List<PaymentGroupMember> paymentGroupMembers) {
		this.setPaymentGroupMembers(paymentGroupMembers);
	}

	public List<PaymentGroupMember> getPaymentGroupMembers()	{
		return paymentGroupMembers;
	}

	public void setPaymentGroupMembers(List<PaymentGroupMember> paymentGroupMembers)	{
		this.paymentGroupMembers = paymentGroupMembers;
	}
}

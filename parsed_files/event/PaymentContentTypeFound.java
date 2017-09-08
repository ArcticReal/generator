package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentContentTypeFound implements Event{

	private List<PaymentContentType> paymentContentTypes;

	public PaymentContentTypeFound(List<PaymentContentType> paymentContentTypes) {
		this.setPaymentContentTypes(paymentContentTypes);
	}

	public List<PaymentContentType> getPaymentContentTypes()	{
		return paymentContentTypes;
	}

	public void setPaymentContentTypes(List<PaymentContentType> paymentContentTypes)	{
		this.paymentContentTypes = paymentContentTypes;
	}
}

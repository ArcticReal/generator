package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentTypeAttrFound implements Event{

	private List<PaymentTypeAttr> paymentTypeAttrs;

	public PaymentTypeAttrFound(List<PaymentTypeAttr> paymentTypeAttrs) {
		this.setPaymentTypeAttrs(paymentTypeAttrs);
	}

	public List<PaymentTypeAttr> getPaymentTypeAttrs()	{
		return paymentTypeAttrs;
	}

	public void setPaymentTypeAttrs(List<PaymentTypeAttr> paymentTypeAttrs)	{
		this.paymentTypeAttrs = paymentTypeAttrs;
	}
}

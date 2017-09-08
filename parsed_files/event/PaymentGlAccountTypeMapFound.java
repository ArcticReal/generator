package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGlAccountTypeMapFound implements Event{

	private List<PaymentGlAccountTypeMap> paymentGlAccountTypeMaps;

	public PaymentGlAccountTypeMapFound(List<PaymentGlAccountTypeMap> paymentGlAccountTypeMaps) {
		this.setPaymentGlAccountTypeMaps(paymentGlAccountTypeMaps);
	}

	public List<PaymentGlAccountTypeMap> getPaymentGlAccountTypeMaps()	{
		return paymentGlAccountTypeMaps;
	}

	public void setPaymentGlAccountTypeMaps(List<PaymentGlAccountTypeMap> paymentGlAccountTypeMaps)	{
		this.paymentGlAccountTypeMaps = paymentGlAccountTypeMaps;
	}
}

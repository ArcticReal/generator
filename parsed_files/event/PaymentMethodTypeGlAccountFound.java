package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentMethodTypeGlAccountFound implements Event{

	private List<PaymentMethodTypeGlAccount> paymentMethodTypeGlAccounts;

	public PaymentMethodTypeGlAccountFound(List<PaymentMethodTypeGlAccount> paymentMethodTypeGlAccounts) {
		this.setPaymentMethodTypeGlAccounts(paymentMethodTypeGlAccounts);
	}

	public List<PaymentMethodTypeGlAccount> getPaymentMethodTypeGlAccounts()	{
		return paymentMethodTypeGlAccounts;
	}

	public void setPaymentMethodTypeGlAccounts(List<PaymentMethodTypeGlAccount> paymentMethodTypeGlAccounts)	{
		this.paymentMethodTypeGlAccounts = paymentMethodTypeGlAccounts;
	}
}

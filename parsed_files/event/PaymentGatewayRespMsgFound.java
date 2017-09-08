package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGatewayRespMsgFound implements Event{

	private List<PaymentGatewayRespMsg> paymentGatewayRespMsgs;

	public PaymentGatewayRespMsgFound(List<PaymentGatewayRespMsg> paymentGatewayRespMsgs) {
		this.setPaymentGatewayRespMsgs(paymentGatewayRespMsgs);
	}

	public List<PaymentGatewayRespMsg> getPaymentGatewayRespMsgs()	{
		return paymentGatewayRespMsgs;
	}

	public void setPaymentGatewayRespMsgs(List<PaymentGatewayRespMsg> paymentGatewayRespMsgs)	{
		this.paymentGatewayRespMsgs = paymentGatewayRespMsgs;
	}
}

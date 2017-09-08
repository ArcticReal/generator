package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CreditCardFound implements Event{

	private List<CreditCard> creditCards;

	public CreditCardFound(List<CreditCard> creditCards) {
		this.setCreditCards(creditCards);
	}

	public List<CreditCard> getCreditCards()	{
		return creditCards;
	}

	public void setCreditCards(List<CreditCard> creditCards)	{
		this.creditCards = creditCards;
	}
}

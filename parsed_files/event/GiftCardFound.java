package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GiftCardFound implements Event{

	private List<GiftCard> giftCards;

	public GiftCardFound(List<GiftCard> giftCards) {
		this.setGiftCards(giftCards);
	}

	public List<GiftCard> getGiftCards()	{
		return giftCards;
	}

	public void setGiftCards(List<GiftCard> giftCards)	{
		this.giftCards = giftCards;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GiftCardFulfillmentFound implements Event{

	private List<GiftCardFulfillment> giftCardFulfillments;

	public GiftCardFulfillmentFound(List<GiftCardFulfillment> giftCardFulfillments) {
		this.setGiftCardFulfillments(giftCardFulfillments);
	}

	public List<GiftCardFulfillment> getGiftCardFulfillments()	{
		return giftCardFulfillments;
	}

	public void setGiftCardFulfillments(List<GiftCardFulfillment> giftCardFulfillments)	{
		this.giftCardFulfillments = giftCardFulfillments;
	}
}

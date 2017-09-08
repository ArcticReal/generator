package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SubscriptionFulfillmentPieceFound implements Event{

	private List<SubscriptionFulfillmentPiece> subscriptionFulfillmentPieces;

	public SubscriptionFulfillmentPieceFound(List<SubscriptionFulfillmentPiece> subscriptionFulfillmentPieces) {
		this.setSubscriptionFulfillmentPieces(subscriptionFulfillmentPieces);
	}

	public List<SubscriptionFulfillmentPiece> getSubscriptionFulfillmentPieces()	{
		return subscriptionFulfillmentPieces;
	}

	public void setSubscriptionFulfillmentPieces(List<SubscriptionFulfillmentPiece> subscriptionFulfillmentPieces)	{
		this.subscriptionFulfillmentPieces = subscriptionFulfillmentPieces;
	}
}

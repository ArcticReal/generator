package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CartAbandonedLineFound implements Event{

	private List<CartAbandonedLine> cartAbandonedLines;

	public CartAbandonedLineFound(List<CartAbandonedLine> cartAbandonedLines) {
		this.setCartAbandonedLines(cartAbandonedLines);
	}

	public List<CartAbandonedLine> getCartAbandonedLines()	{
		return cartAbandonedLines;
	}

	public void setCartAbandonedLines(List<CartAbandonedLine> cartAbandonedLines)	{
		this.cartAbandonedLines = cartAbandonedLines;
	}
}

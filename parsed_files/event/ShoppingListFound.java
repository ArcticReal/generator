package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShoppingListFound implements Event{

	private List<ShoppingList> shoppingLists;

	public ShoppingListFound(List<ShoppingList> shoppingLists) {
		this.setShoppingLists(shoppingLists);
	}

	public List<ShoppingList> getShoppingLists()	{
		return shoppingLists;
	}

	public void setShoppingLists(List<ShoppingList> shoppingLists)	{
		this.shoppingLists = shoppingLists;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShoppingListWorkEffortFound implements Event{

	private List<ShoppingListWorkEffort> shoppingListWorkEfforts;

	public ShoppingListWorkEffortFound(List<ShoppingListWorkEffort> shoppingListWorkEfforts) {
		this.setShoppingListWorkEfforts(shoppingListWorkEfforts);
	}

	public List<ShoppingListWorkEffort> getShoppingListWorkEfforts()	{
		return shoppingListWorkEfforts;
	}

	public void setShoppingListWorkEfforts(List<ShoppingListWorkEffort> shoppingListWorkEfforts)	{
		this.shoppingListWorkEfforts = shoppingListWorkEfforts;
	}
}

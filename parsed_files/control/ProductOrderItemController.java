package com.skytala.eCommerce.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Splitter;
import com.skytala.eCommerce.command.AddProductOrderItem;
import com.skytala.eCommerce.command.DeleteProductOrderItem;
import com.skytala.eCommerce.command.UpdateProductOrderItem;
import com.skytala.eCommerce.entity.ProductOrderItem;
import com.skytala.eCommerce.entity.ProductOrderItemMapper;
import com.skytala.eCommerce.event.ProductOrderItemAdded;
import com.skytala.eCommerce.event.ProductOrderItemDeleted;
import com.skytala.eCommerce.event.ProductOrderItemFound;
import com.skytala.eCommerce.event.ProductOrderItemUpdated;
import com.skytala.eCommerce.query.FindProductOrderItemsBy;

@RestController
@RequestMapping("/api/productOrderItem")
public class ProductOrderItemController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductOrderItem>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductOrderItemController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductOrderItem
	 * @return a List with the ProductOrderItems
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductOrderItem> findProductOrderItemsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductOrderItemsBy query = new FindProductOrderItemsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductOrderItemController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductOrderItemFound.class,
				event -> sendProductOrderItemsFoundMessage(((ProductOrderItemFound) event).getProductOrderItems(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductOrderItemsFoundMessage(List<ProductOrderItem> productOrderItems, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productOrderItems);
	}

	/**
	 * 
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @return true on success; false on fail
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/add", consumes = "application/x-www-form-urlencoded")
	public boolean createProductOrderItem(HttpServletRequest request) {

		ProductOrderItem productOrderItemToBeAdded = new ProductOrderItem();
		try {
			productOrderItemToBeAdded = ProductOrderItemMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductOrderItem(productOrderItemToBeAdded);

	}

	/**
	 * creates a new ProductOrderItem entry in the ofbiz database
	 * 
	 * @param productOrderItemToBeAdded
	 *            the ProductOrderItem thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductOrderItem(ProductOrderItem productOrderItemToBeAdded) {

		AddProductOrderItem com = new AddProductOrderItem(productOrderItemToBeAdded);
		int usedTicketId;

		synchronized (ProductOrderItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductOrderItemAdded.class,
				event -> sendProductOrderItemChangedMessage(((ProductOrderItemAdded) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);

	}

	/**
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request HttpServletRequest object
	 * @return true on success, false on fail
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/update", consumes = "application/x-www-form-urlencoded")
	public boolean updateProductOrderItem(HttpServletRequest request) {

		BufferedReader br;
		String data = null;
		Map<String, String> dataMap = null;

		try {
			br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			if (br != null) {
				data = java.net.URLDecoder.decode(br.readLine(), "UTF-8");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		dataMap = Splitter.on('&').trimResults().withKeyValueSeparator(Splitter.on('=').limit(2).trimResults())
				.split(data);

		ProductOrderItem productOrderItemToBeUpdated = new ProductOrderItem();

		try {
			productOrderItemToBeUpdated = ProductOrderItemMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductOrderItem(productOrderItemToBeUpdated);

	}

	/**
	 * Updates the ProductOrderItem with the specific Id
	 * 
	 * @param productOrderItemToBeUpdated the ProductOrderItem thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductOrderItem(ProductOrderItem productOrderItemToBeUpdated) {

		UpdateProductOrderItem com = new UpdateProductOrderItem(productOrderItemToBeUpdated);

		int usedTicketId;

		synchronized (ProductOrderItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductOrderItemUpdated.class,
				event -> sendProductOrderItemChangedMessage(((ProductOrderItemUpdated) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	/**
	 * removes a ProductOrderItem from the database
	 * 
	 * @param productOrderItemId:
	 *            the id of the ProductOrderItem thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductOrderItemById(@RequestParam(value = "productOrderItemId") String productOrderItemId) {

		DeleteProductOrderItem com = new DeleteProductOrderItem(productOrderItemId);

		int usedTicketId;

		synchronized (ProductOrderItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductOrderItemDeleted.class,
				event -> sendProductOrderItemChangedMessage(((ProductOrderItemDeleted) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	public void sendProductOrderItemChangedMessage(boolean success, int usedTicketId) {
		commandReturnVal.put(usedTicketId, success);
	}

	@RequestMapping(value = (" * "))
	public String returnErrorPage(HttpServletRequest request) {

		String usedUri = request.getRequestURI();
		String[] splittedString = usedUri.split("/");

		String usedRequest = splittedString[splittedString.length - 1];

		if (validRequests.containsKey(usedRequest)) {
			return "Error: request method " + request.getMethod() + " not allowed for \"" + usedUri + "\"!\n"
					+ "Please use " + validRequests.get(usedRequest) + "!";

		}

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productOrderItem/\" plus one of the following: "
				+ "";

		Set<String> keySet = validRequests.keySet();
		Iterator<String> it = keySet.iterator();

		while (it.hasNext()) {
			returnVal += "\"" + it.next() + "\"";
			if (it.hasNext())
				returnVal += ", ";
		}

		returnVal += "!";

		return returnVal;

	}
}

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
import com.skytala.eCommerce.command.AddProductGroupOrder;
import com.skytala.eCommerce.command.DeleteProductGroupOrder;
import com.skytala.eCommerce.command.UpdateProductGroupOrder;
import com.skytala.eCommerce.entity.ProductGroupOrder;
import com.skytala.eCommerce.entity.ProductGroupOrderMapper;
import com.skytala.eCommerce.event.ProductGroupOrderAdded;
import com.skytala.eCommerce.event.ProductGroupOrderDeleted;
import com.skytala.eCommerce.event.ProductGroupOrderFound;
import com.skytala.eCommerce.event.ProductGroupOrderUpdated;
import com.skytala.eCommerce.query.FindProductGroupOrdersBy;

@RestController
@RequestMapping("/api/productGroupOrder")
public class ProductGroupOrderController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductGroupOrder>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductGroupOrderController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductGroupOrder
	 * @return a List with the ProductGroupOrders
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductGroupOrder> findProductGroupOrdersBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductGroupOrdersBy query = new FindProductGroupOrdersBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductGroupOrderController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductGroupOrderFound.class,
				event -> sendProductGroupOrdersFoundMessage(((ProductGroupOrderFound) event).getProductGroupOrders(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductGroupOrdersFoundMessage(List<ProductGroupOrder> productGroupOrders, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productGroupOrders);
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
	public boolean createProductGroupOrder(HttpServletRequest request) {

		ProductGroupOrder productGroupOrderToBeAdded = new ProductGroupOrder();
		try {
			productGroupOrderToBeAdded = ProductGroupOrderMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductGroupOrder(productGroupOrderToBeAdded);

	}

	/**
	 * creates a new ProductGroupOrder entry in the ofbiz database
	 * 
	 * @param productGroupOrderToBeAdded
	 *            the ProductGroupOrder thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductGroupOrder(ProductGroupOrder productGroupOrderToBeAdded) {

		AddProductGroupOrder com = new AddProductGroupOrder(productGroupOrderToBeAdded);
		int usedTicketId;

		synchronized (ProductGroupOrderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductGroupOrderAdded.class,
				event -> sendProductGroupOrderChangedMessage(((ProductGroupOrderAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductGroupOrder(HttpServletRequest request) {

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

		ProductGroupOrder productGroupOrderToBeUpdated = new ProductGroupOrder();

		try {
			productGroupOrderToBeUpdated = ProductGroupOrderMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductGroupOrder(productGroupOrderToBeUpdated);

	}

	/**
	 * Updates the ProductGroupOrder with the specific Id
	 * 
	 * @param productGroupOrderToBeUpdated the ProductGroupOrder thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductGroupOrder(ProductGroupOrder productGroupOrderToBeUpdated) {

		UpdateProductGroupOrder com = new UpdateProductGroupOrder(productGroupOrderToBeUpdated);

		int usedTicketId;

		synchronized (ProductGroupOrderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductGroupOrderUpdated.class,
				event -> sendProductGroupOrderChangedMessage(((ProductGroupOrderUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductGroupOrder from the database
	 * 
	 * @param productGroupOrderId:
	 *            the id of the ProductGroupOrder thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductGroupOrderById(@RequestParam(value = "productGroupOrderId") String productGroupOrderId) {

		DeleteProductGroupOrder com = new DeleteProductGroupOrder(productGroupOrderId);

		int usedTicketId;

		synchronized (ProductGroupOrderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductGroupOrderDeleted.class,
				event -> sendProductGroupOrderChangedMessage(((ProductGroupOrderDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductGroupOrderChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productGroupOrder/\" plus one of the following: "
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

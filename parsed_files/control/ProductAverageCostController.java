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
import com.skytala.eCommerce.command.AddProductAverageCost;
import com.skytala.eCommerce.command.DeleteProductAverageCost;
import com.skytala.eCommerce.command.UpdateProductAverageCost;
import com.skytala.eCommerce.entity.ProductAverageCost;
import com.skytala.eCommerce.entity.ProductAverageCostMapper;
import com.skytala.eCommerce.event.ProductAverageCostAdded;
import com.skytala.eCommerce.event.ProductAverageCostDeleted;
import com.skytala.eCommerce.event.ProductAverageCostFound;
import com.skytala.eCommerce.event.ProductAverageCostUpdated;
import com.skytala.eCommerce.query.FindProductAverageCostsBy;

@RestController
@RequestMapping("/api/productAverageCost")
public class ProductAverageCostController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductAverageCost>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductAverageCostController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductAverageCost
	 * @return a List with the ProductAverageCosts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductAverageCost> findProductAverageCostsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductAverageCostsBy query = new FindProductAverageCostsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductAverageCostController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAverageCostFound.class,
				event -> sendProductAverageCostsFoundMessage(((ProductAverageCostFound) event).getProductAverageCosts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductAverageCostsFoundMessage(List<ProductAverageCost> productAverageCosts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productAverageCosts);
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
	public boolean createProductAverageCost(HttpServletRequest request) {

		ProductAverageCost productAverageCostToBeAdded = new ProductAverageCost();
		try {
			productAverageCostToBeAdded = ProductAverageCostMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductAverageCost(productAverageCostToBeAdded);

	}

	/**
	 * creates a new ProductAverageCost entry in the ofbiz database
	 * 
	 * @param productAverageCostToBeAdded
	 *            the ProductAverageCost thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductAverageCost(ProductAverageCost productAverageCostToBeAdded) {

		AddProductAverageCost com = new AddProductAverageCost(productAverageCostToBeAdded);
		int usedTicketId;

		synchronized (ProductAverageCostController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAverageCostAdded.class,
				event -> sendProductAverageCostChangedMessage(((ProductAverageCostAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductAverageCost(HttpServletRequest request) {

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

		ProductAverageCost productAverageCostToBeUpdated = new ProductAverageCost();

		try {
			productAverageCostToBeUpdated = ProductAverageCostMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductAverageCost(productAverageCostToBeUpdated);

	}

	/**
	 * Updates the ProductAverageCost with the specific Id
	 * 
	 * @param productAverageCostToBeUpdated the ProductAverageCost thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductAverageCost(ProductAverageCost productAverageCostToBeUpdated) {

		UpdateProductAverageCost com = new UpdateProductAverageCost(productAverageCostToBeUpdated);

		int usedTicketId;

		synchronized (ProductAverageCostController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAverageCostUpdated.class,
				event -> sendProductAverageCostChangedMessage(((ProductAverageCostUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductAverageCost from the database
	 * 
	 * @param productAverageCostId:
	 *            the id of the ProductAverageCost thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductAverageCostById(@RequestParam(value = "productAverageCostId") String productAverageCostId) {

		DeleteProductAverageCost com = new DeleteProductAverageCost(productAverageCostId);

		int usedTicketId;

		synchronized (ProductAverageCostController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAverageCostDeleted.class,
				event -> sendProductAverageCostChangedMessage(((ProductAverageCostDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductAverageCostChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productAverageCost/\" plus one of the following: "
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

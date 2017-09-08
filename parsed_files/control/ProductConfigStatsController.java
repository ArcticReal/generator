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
import com.skytala.eCommerce.command.AddProductConfigStats;
import com.skytala.eCommerce.command.DeleteProductConfigStats;
import com.skytala.eCommerce.command.UpdateProductConfigStats;
import com.skytala.eCommerce.entity.ProductConfigStats;
import com.skytala.eCommerce.entity.ProductConfigStatsMapper;
import com.skytala.eCommerce.event.ProductConfigStatsAdded;
import com.skytala.eCommerce.event.ProductConfigStatsDeleted;
import com.skytala.eCommerce.event.ProductConfigStatsFound;
import com.skytala.eCommerce.event.ProductConfigStatsUpdated;
import com.skytala.eCommerce.query.FindProductConfigStatssBy;

@RestController
@RequestMapping("/api/productConfigStats")
public class ProductConfigStatsController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductConfigStats>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductConfigStatsController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductConfigStats
	 * @return a List with the ProductConfigStatss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductConfigStats> findProductConfigStatssBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductConfigStatssBy query = new FindProductConfigStatssBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductConfigStatsController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigStatsFound.class,
				event -> sendProductConfigStatssFoundMessage(((ProductConfigStatsFound) event).getProductConfigStatss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductConfigStatssFoundMessage(List<ProductConfigStats> productConfigStatss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productConfigStatss);
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
	public boolean createProductConfigStats(HttpServletRequest request) {

		ProductConfigStats productConfigStatsToBeAdded = new ProductConfigStats();
		try {
			productConfigStatsToBeAdded = ProductConfigStatsMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductConfigStats(productConfigStatsToBeAdded);

	}

	/**
	 * creates a new ProductConfigStats entry in the ofbiz database
	 * 
	 * @param productConfigStatsToBeAdded
	 *            the ProductConfigStats thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductConfigStats(ProductConfigStats productConfigStatsToBeAdded) {

		AddProductConfigStats com = new AddProductConfigStats(productConfigStatsToBeAdded);
		int usedTicketId;

		synchronized (ProductConfigStatsController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigStatsAdded.class,
				event -> sendProductConfigStatsChangedMessage(((ProductConfigStatsAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductConfigStats(HttpServletRequest request) {

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

		ProductConfigStats productConfigStatsToBeUpdated = new ProductConfigStats();

		try {
			productConfigStatsToBeUpdated = ProductConfigStatsMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductConfigStats(productConfigStatsToBeUpdated);

	}

	/**
	 * Updates the ProductConfigStats with the specific Id
	 * 
	 * @param productConfigStatsToBeUpdated the ProductConfigStats thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductConfigStats(ProductConfigStats productConfigStatsToBeUpdated) {

		UpdateProductConfigStats com = new UpdateProductConfigStats(productConfigStatsToBeUpdated);

		int usedTicketId;

		synchronized (ProductConfigStatsController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigStatsUpdated.class,
				event -> sendProductConfigStatsChangedMessage(((ProductConfigStatsUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductConfigStats from the database
	 * 
	 * @param productConfigStatsId:
	 *            the id of the ProductConfigStats thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductConfigStatsById(@RequestParam(value = "productConfigStatsId") String productConfigStatsId) {

		DeleteProductConfigStats com = new DeleteProductConfigStats(productConfigStatsId);

		int usedTicketId;

		synchronized (ProductConfigStatsController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigStatsDeleted.class,
				event -> sendProductConfigStatsChangedMessage(((ProductConfigStatsDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductConfigStatsChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productConfigStats/\" plus one of the following: "
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

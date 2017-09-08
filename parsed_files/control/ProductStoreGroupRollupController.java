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
import com.skytala.eCommerce.command.AddProductStoreGroupRollup;
import com.skytala.eCommerce.command.DeleteProductStoreGroupRollup;
import com.skytala.eCommerce.command.UpdateProductStoreGroupRollup;
import com.skytala.eCommerce.entity.ProductStoreGroupRollup;
import com.skytala.eCommerce.entity.ProductStoreGroupRollupMapper;
import com.skytala.eCommerce.event.ProductStoreGroupRollupAdded;
import com.skytala.eCommerce.event.ProductStoreGroupRollupDeleted;
import com.skytala.eCommerce.event.ProductStoreGroupRollupFound;
import com.skytala.eCommerce.event.ProductStoreGroupRollupUpdated;
import com.skytala.eCommerce.query.FindProductStoreGroupRollupsBy;

@RestController
@RequestMapping("/api/productStoreGroupRollup")
public class ProductStoreGroupRollupController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductStoreGroupRollup>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductStoreGroupRollupController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductStoreGroupRollup
	 * @return a List with the ProductStoreGroupRollups
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductStoreGroupRollup> findProductStoreGroupRollupsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductStoreGroupRollupsBy query = new FindProductStoreGroupRollupsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductStoreGroupRollupController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupRollupFound.class,
				event -> sendProductStoreGroupRollupsFoundMessage(((ProductStoreGroupRollupFound) event).getProductStoreGroupRollups(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductStoreGroupRollupsFoundMessage(List<ProductStoreGroupRollup> productStoreGroupRollups, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productStoreGroupRollups);
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
	public boolean createProductStoreGroupRollup(HttpServletRequest request) {

		ProductStoreGroupRollup productStoreGroupRollupToBeAdded = new ProductStoreGroupRollup();
		try {
			productStoreGroupRollupToBeAdded = ProductStoreGroupRollupMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductStoreGroupRollup(productStoreGroupRollupToBeAdded);

	}

	/**
	 * creates a new ProductStoreGroupRollup entry in the ofbiz database
	 * 
	 * @param productStoreGroupRollupToBeAdded
	 *            the ProductStoreGroupRollup thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductStoreGroupRollup(ProductStoreGroupRollup productStoreGroupRollupToBeAdded) {

		AddProductStoreGroupRollup com = new AddProductStoreGroupRollup(productStoreGroupRollupToBeAdded);
		int usedTicketId;

		synchronized (ProductStoreGroupRollupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupRollupAdded.class,
				event -> sendProductStoreGroupRollupChangedMessage(((ProductStoreGroupRollupAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductStoreGroupRollup(HttpServletRequest request) {

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

		ProductStoreGroupRollup productStoreGroupRollupToBeUpdated = new ProductStoreGroupRollup();

		try {
			productStoreGroupRollupToBeUpdated = ProductStoreGroupRollupMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductStoreGroupRollup(productStoreGroupRollupToBeUpdated);

	}

	/**
	 * Updates the ProductStoreGroupRollup with the specific Id
	 * 
	 * @param productStoreGroupRollupToBeUpdated the ProductStoreGroupRollup thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductStoreGroupRollup(ProductStoreGroupRollup productStoreGroupRollupToBeUpdated) {

		UpdateProductStoreGroupRollup com = new UpdateProductStoreGroupRollup(productStoreGroupRollupToBeUpdated);

		int usedTicketId;

		synchronized (ProductStoreGroupRollupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupRollupUpdated.class,
				event -> sendProductStoreGroupRollupChangedMessage(((ProductStoreGroupRollupUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductStoreGroupRollup from the database
	 * 
	 * @param productStoreGroupRollupId:
	 *            the id of the ProductStoreGroupRollup thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductStoreGroupRollupById(@RequestParam(value = "productStoreGroupRollupId") String productStoreGroupRollupId) {

		DeleteProductStoreGroupRollup com = new DeleteProductStoreGroupRollup(productStoreGroupRollupId);

		int usedTicketId;

		synchronized (ProductStoreGroupRollupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreGroupRollupDeleted.class,
				event -> sendProductStoreGroupRollupChangedMessage(((ProductStoreGroupRollupDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductStoreGroupRollupChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productStoreGroupRollup/\" plus one of the following: "
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

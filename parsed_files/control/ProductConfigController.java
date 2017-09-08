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
import com.skytala.eCommerce.command.AddProductConfig;
import com.skytala.eCommerce.command.DeleteProductConfig;
import com.skytala.eCommerce.command.UpdateProductConfig;
import com.skytala.eCommerce.entity.ProductConfig;
import com.skytala.eCommerce.entity.ProductConfigMapper;
import com.skytala.eCommerce.event.ProductConfigAdded;
import com.skytala.eCommerce.event.ProductConfigDeleted;
import com.skytala.eCommerce.event.ProductConfigFound;
import com.skytala.eCommerce.event.ProductConfigUpdated;
import com.skytala.eCommerce.query.FindProductConfigsBy;

@RestController
@RequestMapping("/api/productConfig")
public class ProductConfigController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductConfig>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductConfigController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductConfig
	 * @return a List with the ProductConfigs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductConfig> findProductConfigsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductConfigsBy query = new FindProductConfigsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductConfigController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigFound.class,
				event -> sendProductConfigsFoundMessage(((ProductConfigFound) event).getProductConfigs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductConfigsFoundMessage(List<ProductConfig> productConfigs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productConfigs);
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
	public boolean createProductConfig(HttpServletRequest request) {

		ProductConfig productConfigToBeAdded = new ProductConfig();
		try {
			productConfigToBeAdded = ProductConfigMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductConfig(productConfigToBeAdded);

	}

	/**
	 * creates a new ProductConfig entry in the ofbiz database
	 * 
	 * @param productConfigToBeAdded
	 *            the ProductConfig thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductConfig(ProductConfig productConfigToBeAdded) {

		AddProductConfig com = new AddProductConfig(productConfigToBeAdded);
		int usedTicketId;

		synchronized (ProductConfigController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigAdded.class,
				event -> sendProductConfigChangedMessage(((ProductConfigAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductConfig(HttpServletRequest request) {

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

		ProductConfig productConfigToBeUpdated = new ProductConfig();

		try {
			productConfigToBeUpdated = ProductConfigMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductConfig(productConfigToBeUpdated);

	}

	/**
	 * Updates the ProductConfig with the specific Id
	 * 
	 * @param productConfigToBeUpdated the ProductConfig thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductConfig(ProductConfig productConfigToBeUpdated) {

		UpdateProductConfig com = new UpdateProductConfig(productConfigToBeUpdated);

		int usedTicketId;

		synchronized (ProductConfigController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigUpdated.class,
				event -> sendProductConfigChangedMessage(((ProductConfigUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductConfig from the database
	 * 
	 * @param productConfigId:
	 *            the id of the ProductConfig thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductConfigById(@RequestParam(value = "productConfigId") String productConfigId) {

		DeleteProductConfig com = new DeleteProductConfig(productConfigId);

		int usedTicketId;

		synchronized (ProductConfigController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigDeleted.class,
				event -> sendProductConfigChangedMessage(((ProductConfigDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductConfigChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productConfig/\" plus one of the following: "
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

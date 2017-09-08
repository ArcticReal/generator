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
import com.skytala.eCommerce.command.AddProductConfigConfig;
import com.skytala.eCommerce.command.DeleteProductConfigConfig;
import com.skytala.eCommerce.command.UpdateProductConfigConfig;
import com.skytala.eCommerce.entity.ProductConfigConfig;
import com.skytala.eCommerce.entity.ProductConfigConfigMapper;
import com.skytala.eCommerce.event.ProductConfigConfigAdded;
import com.skytala.eCommerce.event.ProductConfigConfigDeleted;
import com.skytala.eCommerce.event.ProductConfigConfigFound;
import com.skytala.eCommerce.event.ProductConfigConfigUpdated;
import com.skytala.eCommerce.query.FindProductConfigConfigsBy;

@RestController
@RequestMapping("/api/productConfigConfig")
public class ProductConfigConfigController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductConfigConfig>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductConfigConfigController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductConfigConfig
	 * @return a List with the ProductConfigConfigs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductConfigConfig> findProductConfigConfigsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductConfigConfigsBy query = new FindProductConfigConfigsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductConfigConfigController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigConfigFound.class,
				event -> sendProductConfigConfigsFoundMessage(((ProductConfigConfigFound) event).getProductConfigConfigs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductConfigConfigsFoundMessage(List<ProductConfigConfig> productConfigConfigs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productConfigConfigs);
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
	public boolean createProductConfigConfig(HttpServletRequest request) {

		ProductConfigConfig productConfigConfigToBeAdded = new ProductConfigConfig();
		try {
			productConfigConfigToBeAdded = ProductConfigConfigMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductConfigConfig(productConfigConfigToBeAdded);

	}

	/**
	 * creates a new ProductConfigConfig entry in the ofbiz database
	 * 
	 * @param productConfigConfigToBeAdded
	 *            the ProductConfigConfig thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductConfigConfig(ProductConfigConfig productConfigConfigToBeAdded) {

		AddProductConfigConfig com = new AddProductConfigConfig(productConfigConfigToBeAdded);
		int usedTicketId;

		synchronized (ProductConfigConfigController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigConfigAdded.class,
				event -> sendProductConfigConfigChangedMessage(((ProductConfigConfigAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductConfigConfig(HttpServletRequest request) {

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

		ProductConfigConfig productConfigConfigToBeUpdated = new ProductConfigConfig();

		try {
			productConfigConfigToBeUpdated = ProductConfigConfigMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductConfigConfig(productConfigConfigToBeUpdated);

	}

	/**
	 * Updates the ProductConfigConfig with the specific Id
	 * 
	 * @param productConfigConfigToBeUpdated the ProductConfigConfig thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductConfigConfig(ProductConfigConfig productConfigConfigToBeUpdated) {

		UpdateProductConfigConfig com = new UpdateProductConfigConfig(productConfigConfigToBeUpdated);

		int usedTicketId;

		synchronized (ProductConfigConfigController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigConfigUpdated.class,
				event -> sendProductConfigConfigChangedMessage(((ProductConfigConfigUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductConfigConfig from the database
	 * 
	 * @param productConfigConfigId:
	 *            the id of the ProductConfigConfig thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductConfigConfigById(@RequestParam(value = "productConfigConfigId") String productConfigConfigId) {

		DeleteProductConfigConfig com = new DeleteProductConfigConfig(productConfigConfigId);

		int usedTicketId;

		synchronized (ProductConfigConfigController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigConfigDeleted.class,
				event -> sendProductConfigConfigChangedMessage(((ProductConfigConfigDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductConfigConfigChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productConfigConfig/\" plus one of the following: "
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

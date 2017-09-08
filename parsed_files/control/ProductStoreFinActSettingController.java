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
import com.skytala.eCommerce.command.AddProductStoreFinActSetting;
import com.skytala.eCommerce.command.DeleteProductStoreFinActSetting;
import com.skytala.eCommerce.command.UpdateProductStoreFinActSetting;
import com.skytala.eCommerce.entity.ProductStoreFinActSetting;
import com.skytala.eCommerce.entity.ProductStoreFinActSettingMapper;
import com.skytala.eCommerce.event.ProductStoreFinActSettingAdded;
import com.skytala.eCommerce.event.ProductStoreFinActSettingDeleted;
import com.skytala.eCommerce.event.ProductStoreFinActSettingFound;
import com.skytala.eCommerce.event.ProductStoreFinActSettingUpdated;
import com.skytala.eCommerce.query.FindProductStoreFinActSettingsBy;

@RestController
@RequestMapping("/api/productStoreFinActSetting")
public class ProductStoreFinActSettingController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductStoreFinActSetting>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductStoreFinActSettingController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductStoreFinActSetting
	 * @return a List with the ProductStoreFinActSettings
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductStoreFinActSetting> findProductStoreFinActSettingsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductStoreFinActSettingsBy query = new FindProductStoreFinActSettingsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductStoreFinActSettingController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreFinActSettingFound.class,
				event -> sendProductStoreFinActSettingsFoundMessage(((ProductStoreFinActSettingFound) event).getProductStoreFinActSettings(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductStoreFinActSettingsFoundMessage(List<ProductStoreFinActSetting> productStoreFinActSettings, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productStoreFinActSettings);
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
	public boolean createProductStoreFinActSetting(HttpServletRequest request) {

		ProductStoreFinActSetting productStoreFinActSettingToBeAdded = new ProductStoreFinActSetting();
		try {
			productStoreFinActSettingToBeAdded = ProductStoreFinActSettingMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductStoreFinActSetting(productStoreFinActSettingToBeAdded);

	}

	/**
	 * creates a new ProductStoreFinActSetting entry in the ofbiz database
	 * 
	 * @param productStoreFinActSettingToBeAdded
	 *            the ProductStoreFinActSetting thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductStoreFinActSetting(ProductStoreFinActSetting productStoreFinActSettingToBeAdded) {

		AddProductStoreFinActSetting com = new AddProductStoreFinActSetting(productStoreFinActSettingToBeAdded);
		int usedTicketId;

		synchronized (ProductStoreFinActSettingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreFinActSettingAdded.class,
				event -> sendProductStoreFinActSettingChangedMessage(((ProductStoreFinActSettingAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductStoreFinActSetting(HttpServletRequest request) {

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

		ProductStoreFinActSetting productStoreFinActSettingToBeUpdated = new ProductStoreFinActSetting();

		try {
			productStoreFinActSettingToBeUpdated = ProductStoreFinActSettingMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductStoreFinActSetting(productStoreFinActSettingToBeUpdated);

	}

	/**
	 * Updates the ProductStoreFinActSetting with the specific Id
	 * 
	 * @param productStoreFinActSettingToBeUpdated the ProductStoreFinActSetting thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductStoreFinActSetting(ProductStoreFinActSetting productStoreFinActSettingToBeUpdated) {

		UpdateProductStoreFinActSetting com = new UpdateProductStoreFinActSetting(productStoreFinActSettingToBeUpdated);

		int usedTicketId;

		synchronized (ProductStoreFinActSettingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreFinActSettingUpdated.class,
				event -> sendProductStoreFinActSettingChangedMessage(((ProductStoreFinActSettingUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductStoreFinActSetting from the database
	 * 
	 * @param productStoreFinActSettingId:
	 *            the id of the ProductStoreFinActSetting thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductStoreFinActSettingById(@RequestParam(value = "productStoreFinActSettingId") String productStoreFinActSettingId) {

		DeleteProductStoreFinActSetting com = new DeleteProductStoreFinActSetting(productStoreFinActSettingId);

		int usedTicketId;

		synchronized (ProductStoreFinActSettingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreFinActSettingDeleted.class,
				event -> sendProductStoreFinActSettingChangedMessage(((ProductStoreFinActSettingDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductStoreFinActSettingChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productStoreFinActSetting/\" plus one of the following: "
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

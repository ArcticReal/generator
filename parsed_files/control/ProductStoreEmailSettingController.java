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
import com.skytala.eCommerce.command.AddProductStoreEmailSetting;
import com.skytala.eCommerce.command.DeleteProductStoreEmailSetting;
import com.skytala.eCommerce.command.UpdateProductStoreEmailSetting;
import com.skytala.eCommerce.entity.ProductStoreEmailSetting;
import com.skytala.eCommerce.entity.ProductStoreEmailSettingMapper;
import com.skytala.eCommerce.event.ProductStoreEmailSettingAdded;
import com.skytala.eCommerce.event.ProductStoreEmailSettingDeleted;
import com.skytala.eCommerce.event.ProductStoreEmailSettingFound;
import com.skytala.eCommerce.event.ProductStoreEmailSettingUpdated;
import com.skytala.eCommerce.query.FindProductStoreEmailSettingsBy;

@RestController
@RequestMapping("/api/productStoreEmailSetting")
public class ProductStoreEmailSettingController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductStoreEmailSetting>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductStoreEmailSettingController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductStoreEmailSetting
	 * @return a List with the ProductStoreEmailSettings
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductStoreEmailSetting> findProductStoreEmailSettingsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductStoreEmailSettingsBy query = new FindProductStoreEmailSettingsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductStoreEmailSettingController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreEmailSettingFound.class,
				event -> sendProductStoreEmailSettingsFoundMessage(((ProductStoreEmailSettingFound) event).getProductStoreEmailSettings(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductStoreEmailSettingsFoundMessage(List<ProductStoreEmailSetting> productStoreEmailSettings, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productStoreEmailSettings);
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
	public boolean createProductStoreEmailSetting(HttpServletRequest request) {

		ProductStoreEmailSetting productStoreEmailSettingToBeAdded = new ProductStoreEmailSetting();
		try {
			productStoreEmailSettingToBeAdded = ProductStoreEmailSettingMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductStoreEmailSetting(productStoreEmailSettingToBeAdded);

	}

	/**
	 * creates a new ProductStoreEmailSetting entry in the ofbiz database
	 * 
	 * @param productStoreEmailSettingToBeAdded
	 *            the ProductStoreEmailSetting thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductStoreEmailSetting(ProductStoreEmailSetting productStoreEmailSettingToBeAdded) {

		AddProductStoreEmailSetting com = new AddProductStoreEmailSetting(productStoreEmailSettingToBeAdded);
		int usedTicketId;

		synchronized (ProductStoreEmailSettingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreEmailSettingAdded.class,
				event -> sendProductStoreEmailSettingChangedMessage(((ProductStoreEmailSettingAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductStoreEmailSetting(HttpServletRequest request) {

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

		ProductStoreEmailSetting productStoreEmailSettingToBeUpdated = new ProductStoreEmailSetting();

		try {
			productStoreEmailSettingToBeUpdated = ProductStoreEmailSettingMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductStoreEmailSetting(productStoreEmailSettingToBeUpdated);

	}

	/**
	 * Updates the ProductStoreEmailSetting with the specific Id
	 * 
	 * @param productStoreEmailSettingToBeUpdated the ProductStoreEmailSetting thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductStoreEmailSetting(ProductStoreEmailSetting productStoreEmailSettingToBeUpdated) {

		UpdateProductStoreEmailSetting com = new UpdateProductStoreEmailSetting(productStoreEmailSettingToBeUpdated);

		int usedTicketId;

		synchronized (ProductStoreEmailSettingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreEmailSettingUpdated.class,
				event -> sendProductStoreEmailSettingChangedMessage(((ProductStoreEmailSettingUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductStoreEmailSetting from the database
	 * 
	 * @param productStoreEmailSettingId:
	 *            the id of the ProductStoreEmailSetting thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductStoreEmailSettingById(@RequestParam(value = "productStoreEmailSettingId") String productStoreEmailSettingId) {

		DeleteProductStoreEmailSetting com = new DeleteProductStoreEmailSetting(productStoreEmailSettingId);

		int usedTicketId;

		synchronized (ProductStoreEmailSettingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreEmailSettingDeleted.class,
				event -> sendProductStoreEmailSettingChangedMessage(((ProductStoreEmailSettingDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductStoreEmailSettingChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productStoreEmailSetting/\" plus one of the following: "
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

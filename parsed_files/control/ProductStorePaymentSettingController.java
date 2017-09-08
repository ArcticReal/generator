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
import com.skytala.eCommerce.command.AddProductStorePaymentSetting;
import com.skytala.eCommerce.command.DeleteProductStorePaymentSetting;
import com.skytala.eCommerce.command.UpdateProductStorePaymentSetting;
import com.skytala.eCommerce.entity.ProductStorePaymentSetting;
import com.skytala.eCommerce.entity.ProductStorePaymentSettingMapper;
import com.skytala.eCommerce.event.ProductStorePaymentSettingAdded;
import com.skytala.eCommerce.event.ProductStorePaymentSettingDeleted;
import com.skytala.eCommerce.event.ProductStorePaymentSettingFound;
import com.skytala.eCommerce.event.ProductStorePaymentSettingUpdated;
import com.skytala.eCommerce.query.FindProductStorePaymentSettingsBy;

@RestController
@RequestMapping("/api/productStorePaymentSetting")
public class ProductStorePaymentSettingController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductStorePaymentSetting>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductStorePaymentSettingController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductStorePaymentSetting
	 * @return a List with the ProductStorePaymentSettings
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductStorePaymentSetting> findProductStorePaymentSettingsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductStorePaymentSettingsBy query = new FindProductStorePaymentSettingsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductStorePaymentSettingController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStorePaymentSettingFound.class,
				event -> sendProductStorePaymentSettingsFoundMessage(((ProductStorePaymentSettingFound) event).getProductStorePaymentSettings(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductStorePaymentSettingsFoundMessage(List<ProductStorePaymentSetting> productStorePaymentSettings, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productStorePaymentSettings);
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
	public boolean createProductStorePaymentSetting(HttpServletRequest request) {

		ProductStorePaymentSetting productStorePaymentSettingToBeAdded = new ProductStorePaymentSetting();
		try {
			productStorePaymentSettingToBeAdded = ProductStorePaymentSettingMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductStorePaymentSetting(productStorePaymentSettingToBeAdded);

	}

	/**
	 * creates a new ProductStorePaymentSetting entry in the ofbiz database
	 * 
	 * @param productStorePaymentSettingToBeAdded
	 *            the ProductStorePaymentSetting thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductStorePaymentSetting(ProductStorePaymentSetting productStorePaymentSettingToBeAdded) {

		AddProductStorePaymentSetting com = new AddProductStorePaymentSetting(productStorePaymentSettingToBeAdded);
		int usedTicketId;

		synchronized (ProductStorePaymentSettingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStorePaymentSettingAdded.class,
				event -> sendProductStorePaymentSettingChangedMessage(((ProductStorePaymentSettingAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductStorePaymentSetting(HttpServletRequest request) {

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

		ProductStorePaymentSetting productStorePaymentSettingToBeUpdated = new ProductStorePaymentSetting();

		try {
			productStorePaymentSettingToBeUpdated = ProductStorePaymentSettingMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductStorePaymentSetting(productStorePaymentSettingToBeUpdated);

	}

	/**
	 * Updates the ProductStorePaymentSetting with the specific Id
	 * 
	 * @param productStorePaymentSettingToBeUpdated the ProductStorePaymentSetting thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductStorePaymentSetting(ProductStorePaymentSetting productStorePaymentSettingToBeUpdated) {

		UpdateProductStorePaymentSetting com = new UpdateProductStorePaymentSetting(productStorePaymentSettingToBeUpdated);

		int usedTicketId;

		synchronized (ProductStorePaymentSettingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStorePaymentSettingUpdated.class,
				event -> sendProductStorePaymentSettingChangedMessage(((ProductStorePaymentSettingUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductStorePaymentSetting from the database
	 * 
	 * @param productStorePaymentSettingId:
	 *            the id of the ProductStorePaymentSetting thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductStorePaymentSettingById(@RequestParam(value = "productStorePaymentSettingId") String productStorePaymentSettingId) {

		DeleteProductStorePaymentSetting com = new DeleteProductStorePaymentSetting(productStorePaymentSettingId);

		int usedTicketId;

		synchronized (ProductStorePaymentSettingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStorePaymentSettingDeleted.class,
				event -> sendProductStorePaymentSettingChangedMessage(((ProductStorePaymentSettingDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductStorePaymentSettingChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productStorePaymentSetting/\" plus one of the following: "
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

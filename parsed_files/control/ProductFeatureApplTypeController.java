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
import com.skytala.eCommerce.command.AddProductFeatureApplType;
import com.skytala.eCommerce.command.DeleteProductFeatureApplType;
import com.skytala.eCommerce.command.UpdateProductFeatureApplType;
import com.skytala.eCommerce.entity.ProductFeatureApplType;
import com.skytala.eCommerce.entity.ProductFeatureApplTypeMapper;
import com.skytala.eCommerce.event.ProductFeatureApplTypeAdded;
import com.skytala.eCommerce.event.ProductFeatureApplTypeDeleted;
import com.skytala.eCommerce.event.ProductFeatureApplTypeFound;
import com.skytala.eCommerce.event.ProductFeatureApplTypeUpdated;
import com.skytala.eCommerce.query.FindProductFeatureApplTypesBy;

@RestController
@RequestMapping("/api/productFeatureApplType")
public class ProductFeatureApplTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductFeatureApplType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductFeatureApplTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductFeatureApplType
	 * @return a List with the ProductFeatureApplTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductFeatureApplType> findProductFeatureApplTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductFeatureApplTypesBy query = new FindProductFeatureApplTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductFeatureApplTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureApplTypeFound.class,
				event -> sendProductFeatureApplTypesFoundMessage(((ProductFeatureApplTypeFound) event).getProductFeatureApplTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductFeatureApplTypesFoundMessage(List<ProductFeatureApplType> productFeatureApplTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productFeatureApplTypes);
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
	public boolean createProductFeatureApplType(HttpServletRequest request) {

		ProductFeatureApplType productFeatureApplTypeToBeAdded = new ProductFeatureApplType();
		try {
			productFeatureApplTypeToBeAdded = ProductFeatureApplTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductFeatureApplType(productFeatureApplTypeToBeAdded);

	}

	/**
	 * creates a new ProductFeatureApplType entry in the ofbiz database
	 * 
	 * @param productFeatureApplTypeToBeAdded
	 *            the ProductFeatureApplType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductFeatureApplType(ProductFeatureApplType productFeatureApplTypeToBeAdded) {

		AddProductFeatureApplType com = new AddProductFeatureApplType(productFeatureApplTypeToBeAdded);
		int usedTicketId;

		synchronized (ProductFeatureApplTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureApplTypeAdded.class,
				event -> sendProductFeatureApplTypeChangedMessage(((ProductFeatureApplTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductFeatureApplType(HttpServletRequest request) {

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

		ProductFeatureApplType productFeatureApplTypeToBeUpdated = new ProductFeatureApplType();

		try {
			productFeatureApplTypeToBeUpdated = ProductFeatureApplTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductFeatureApplType(productFeatureApplTypeToBeUpdated);

	}

	/**
	 * Updates the ProductFeatureApplType with the specific Id
	 * 
	 * @param productFeatureApplTypeToBeUpdated the ProductFeatureApplType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductFeatureApplType(ProductFeatureApplType productFeatureApplTypeToBeUpdated) {

		UpdateProductFeatureApplType com = new UpdateProductFeatureApplType(productFeatureApplTypeToBeUpdated);

		int usedTicketId;

		synchronized (ProductFeatureApplTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureApplTypeUpdated.class,
				event -> sendProductFeatureApplTypeChangedMessage(((ProductFeatureApplTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductFeatureApplType from the database
	 * 
	 * @param productFeatureApplTypeId:
	 *            the id of the ProductFeatureApplType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductFeatureApplTypeById(@RequestParam(value = "productFeatureApplTypeId") String productFeatureApplTypeId) {

		DeleteProductFeatureApplType com = new DeleteProductFeatureApplType(productFeatureApplTypeId);

		int usedTicketId;

		synchronized (ProductFeatureApplTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureApplTypeDeleted.class,
				event -> sendProductFeatureApplTypeChangedMessage(((ProductFeatureApplTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductFeatureApplTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productFeatureApplType/\" plus one of the following: "
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

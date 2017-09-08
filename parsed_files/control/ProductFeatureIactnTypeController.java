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
import com.skytala.eCommerce.command.AddProductFeatureIactnType;
import com.skytala.eCommerce.command.DeleteProductFeatureIactnType;
import com.skytala.eCommerce.command.UpdateProductFeatureIactnType;
import com.skytala.eCommerce.entity.ProductFeatureIactnType;
import com.skytala.eCommerce.entity.ProductFeatureIactnTypeMapper;
import com.skytala.eCommerce.event.ProductFeatureIactnTypeAdded;
import com.skytala.eCommerce.event.ProductFeatureIactnTypeDeleted;
import com.skytala.eCommerce.event.ProductFeatureIactnTypeFound;
import com.skytala.eCommerce.event.ProductFeatureIactnTypeUpdated;
import com.skytala.eCommerce.query.FindProductFeatureIactnTypesBy;

@RestController
@RequestMapping("/api/productFeatureIactnType")
public class ProductFeatureIactnTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductFeatureIactnType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductFeatureIactnTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductFeatureIactnType
	 * @return a List with the ProductFeatureIactnTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductFeatureIactnType> findProductFeatureIactnTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductFeatureIactnTypesBy query = new FindProductFeatureIactnTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductFeatureIactnTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureIactnTypeFound.class,
				event -> sendProductFeatureIactnTypesFoundMessage(((ProductFeatureIactnTypeFound) event).getProductFeatureIactnTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductFeatureIactnTypesFoundMessage(List<ProductFeatureIactnType> productFeatureIactnTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productFeatureIactnTypes);
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
	public boolean createProductFeatureIactnType(HttpServletRequest request) {

		ProductFeatureIactnType productFeatureIactnTypeToBeAdded = new ProductFeatureIactnType();
		try {
			productFeatureIactnTypeToBeAdded = ProductFeatureIactnTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductFeatureIactnType(productFeatureIactnTypeToBeAdded);

	}

	/**
	 * creates a new ProductFeatureIactnType entry in the ofbiz database
	 * 
	 * @param productFeatureIactnTypeToBeAdded
	 *            the ProductFeatureIactnType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductFeatureIactnType(ProductFeatureIactnType productFeatureIactnTypeToBeAdded) {

		AddProductFeatureIactnType com = new AddProductFeatureIactnType(productFeatureIactnTypeToBeAdded);
		int usedTicketId;

		synchronized (ProductFeatureIactnTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureIactnTypeAdded.class,
				event -> sendProductFeatureIactnTypeChangedMessage(((ProductFeatureIactnTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductFeatureIactnType(HttpServletRequest request) {

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

		ProductFeatureIactnType productFeatureIactnTypeToBeUpdated = new ProductFeatureIactnType();

		try {
			productFeatureIactnTypeToBeUpdated = ProductFeatureIactnTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductFeatureIactnType(productFeatureIactnTypeToBeUpdated);

	}

	/**
	 * Updates the ProductFeatureIactnType with the specific Id
	 * 
	 * @param productFeatureIactnTypeToBeUpdated the ProductFeatureIactnType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductFeatureIactnType(ProductFeatureIactnType productFeatureIactnTypeToBeUpdated) {

		UpdateProductFeatureIactnType com = new UpdateProductFeatureIactnType(productFeatureIactnTypeToBeUpdated);

		int usedTicketId;

		synchronized (ProductFeatureIactnTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureIactnTypeUpdated.class,
				event -> sendProductFeatureIactnTypeChangedMessage(((ProductFeatureIactnTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductFeatureIactnType from the database
	 * 
	 * @param productFeatureIactnTypeId:
	 *            the id of the ProductFeatureIactnType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductFeatureIactnTypeById(@RequestParam(value = "productFeatureIactnTypeId") String productFeatureIactnTypeId) {

		DeleteProductFeatureIactnType com = new DeleteProductFeatureIactnType(productFeatureIactnTypeId);

		int usedTicketId;

		synchronized (ProductFeatureIactnTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureIactnTypeDeleted.class,
				event -> sendProductFeatureIactnTypeChangedMessage(((ProductFeatureIactnTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductFeatureIactnTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productFeatureIactnType/\" plus one of the following: "
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

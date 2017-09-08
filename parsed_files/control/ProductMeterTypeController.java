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
import com.skytala.eCommerce.command.AddProductMeterType;
import com.skytala.eCommerce.command.DeleteProductMeterType;
import com.skytala.eCommerce.command.UpdateProductMeterType;
import com.skytala.eCommerce.entity.ProductMeterType;
import com.skytala.eCommerce.entity.ProductMeterTypeMapper;
import com.skytala.eCommerce.event.ProductMeterTypeAdded;
import com.skytala.eCommerce.event.ProductMeterTypeDeleted;
import com.skytala.eCommerce.event.ProductMeterTypeFound;
import com.skytala.eCommerce.event.ProductMeterTypeUpdated;
import com.skytala.eCommerce.query.FindProductMeterTypesBy;

@RestController
@RequestMapping("/api/productMeterType")
public class ProductMeterTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductMeterType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductMeterTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductMeterType
	 * @return a List with the ProductMeterTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductMeterType> findProductMeterTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductMeterTypesBy query = new FindProductMeterTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductMeterTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductMeterTypeFound.class,
				event -> sendProductMeterTypesFoundMessage(((ProductMeterTypeFound) event).getProductMeterTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductMeterTypesFoundMessage(List<ProductMeterType> productMeterTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productMeterTypes);
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
	public boolean createProductMeterType(HttpServletRequest request) {

		ProductMeterType productMeterTypeToBeAdded = new ProductMeterType();
		try {
			productMeterTypeToBeAdded = ProductMeterTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductMeterType(productMeterTypeToBeAdded);

	}

	/**
	 * creates a new ProductMeterType entry in the ofbiz database
	 * 
	 * @param productMeterTypeToBeAdded
	 *            the ProductMeterType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductMeterType(ProductMeterType productMeterTypeToBeAdded) {

		AddProductMeterType com = new AddProductMeterType(productMeterTypeToBeAdded);
		int usedTicketId;

		synchronized (ProductMeterTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductMeterTypeAdded.class,
				event -> sendProductMeterTypeChangedMessage(((ProductMeterTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductMeterType(HttpServletRequest request) {

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

		ProductMeterType productMeterTypeToBeUpdated = new ProductMeterType();

		try {
			productMeterTypeToBeUpdated = ProductMeterTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductMeterType(productMeterTypeToBeUpdated);

	}

	/**
	 * Updates the ProductMeterType with the specific Id
	 * 
	 * @param productMeterTypeToBeUpdated the ProductMeterType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductMeterType(ProductMeterType productMeterTypeToBeUpdated) {

		UpdateProductMeterType com = new UpdateProductMeterType(productMeterTypeToBeUpdated);

		int usedTicketId;

		synchronized (ProductMeterTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductMeterTypeUpdated.class,
				event -> sendProductMeterTypeChangedMessage(((ProductMeterTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductMeterType from the database
	 * 
	 * @param productMeterTypeId:
	 *            the id of the ProductMeterType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductMeterTypeById(@RequestParam(value = "productMeterTypeId") String productMeterTypeId) {

		DeleteProductMeterType com = new DeleteProductMeterType(productMeterTypeId);

		int usedTicketId;

		synchronized (ProductMeterTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductMeterTypeDeleted.class,
				event -> sendProductMeterTypeChangedMessage(((ProductMeterTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductMeterTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productMeterType/\" plus one of the following: "
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

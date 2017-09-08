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
import com.skytala.eCommerce.command.AddProductMaintType;
import com.skytala.eCommerce.command.DeleteProductMaintType;
import com.skytala.eCommerce.command.UpdateProductMaintType;
import com.skytala.eCommerce.entity.ProductMaintType;
import com.skytala.eCommerce.entity.ProductMaintTypeMapper;
import com.skytala.eCommerce.event.ProductMaintTypeAdded;
import com.skytala.eCommerce.event.ProductMaintTypeDeleted;
import com.skytala.eCommerce.event.ProductMaintTypeFound;
import com.skytala.eCommerce.event.ProductMaintTypeUpdated;
import com.skytala.eCommerce.query.FindProductMaintTypesBy;

@RestController
@RequestMapping("/api/productMaintType")
public class ProductMaintTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductMaintType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductMaintTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductMaintType
	 * @return a List with the ProductMaintTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductMaintType> findProductMaintTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductMaintTypesBy query = new FindProductMaintTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductMaintTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductMaintTypeFound.class,
				event -> sendProductMaintTypesFoundMessage(((ProductMaintTypeFound) event).getProductMaintTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductMaintTypesFoundMessage(List<ProductMaintType> productMaintTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productMaintTypes);
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
	public boolean createProductMaintType(HttpServletRequest request) {

		ProductMaintType productMaintTypeToBeAdded = new ProductMaintType();
		try {
			productMaintTypeToBeAdded = ProductMaintTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductMaintType(productMaintTypeToBeAdded);

	}

	/**
	 * creates a new ProductMaintType entry in the ofbiz database
	 * 
	 * @param productMaintTypeToBeAdded
	 *            the ProductMaintType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductMaintType(ProductMaintType productMaintTypeToBeAdded) {

		AddProductMaintType com = new AddProductMaintType(productMaintTypeToBeAdded);
		int usedTicketId;

		synchronized (ProductMaintTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductMaintTypeAdded.class,
				event -> sendProductMaintTypeChangedMessage(((ProductMaintTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductMaintType(HttpServletRequest request) {

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

		ProductMaintType productMaintTypeToBeUpdated = new ProductMaintType();

		try {
			productMaintTypeToBeUpdated = ProductMaintTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductMaintType(productMaintTypeToBeUpdated);

	}

	/**
	 * Updates the ProductMaintType with the specific Id
	 * 
	 * @param productMaintTypeToBeUpdated the ProductMaintType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductMaintType(ProductMaintType productMaintTypeToBeUpdated) {

		UpdateProductMaintType com = new UpdateProductMaintType(productMaintTypeToBeUpdated);

		int usedTicketId;

		synchronized (ProductMaintTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductMaintTypeUpdated.class,
				event -> sendProductMaintTypeChangedMessage(((ProductMaintTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductMaintType from the database
	 * 
	 * @param productMaintTypeId:
	 *            the id of the ProductMaintType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductMaintTypeById(@RequestParam(value = "productMaintTypeId") String productMaintTypeId) {

		DeleteProductMaintType com = new DeleteProductMaintType(productMaintTypeId);

		int usedTicketId;

		synchronized (ProductMaintTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductMaintTypeDeleted.class,
				event -> sendProductMaintTypeChangedMessage(((ProductMaintTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductMaintTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productMaintType/\" plus one of the following: "
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

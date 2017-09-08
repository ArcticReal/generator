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
import com.skytala.eCommerce.command.AddProductType;
import com.skytala.eCommerce.command.DeleteProductType;
import com.skytala.eCommerce.command.UpdateProductType;
import com.skytala.eCommerce.entity.ProductType;
import com.skytala.eCommerce.entity.ProductTypeMapper;
import com.skytala.eCommerce.event.ProductTypeAdded;
import com.skytala.eCommerce.event.ProductTypeDeleted;
import com.skytala.eCommerce.event.ProductTypeFound;
import com.skytala.eCommerce.event.ProductTypeUpdated;
import com.skytala.eCommerce.query.FindProductTypesBy;

@RestController
@RequestMapping("/api/productType")
public class ProductTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductType
	 * @return a List with the ProductTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductType> findProductTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductTypesBy query = new FindProductTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductTypeFound.class,
				event -> sendProductTypesFoundMessage(((ProductTypeFound) event).getProductTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductTypesFoundMessage(List<ProductType> productTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productTypes);
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
	public boolean createProductType(HttpServletRequest request) {

		ProductType productTypeToBeAdded = new ProductType();
		try {
			productTypeToBeAdded = ProductTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductType(productTypeToBeAdded);

	}

	/**
	 * creates a new ProductType entry in the ofbiz database
	 * 
	 * @param productTypeToBeAdded
	 *            the ProductType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductType(ProductType productTypeToBeAdded) {

		AddProductType com = new AddProductType(productTypeToBeAdded);
		int usedTicketId;

		synchronized (ProductTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductTypeAdded.class,
				event -> sendProductTypeChangedMessage(((ProductTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductType(HttpServletRequest request) {

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

		ProductType productTypeToBeUpdated = new ProductType();

		try {
			productTypeToBeUpdated = ProductTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductType(productTypeToBeUpdated);

	}

	/**
	 * Updates the ProductType with the specific Id
	 * 
	 * @param productTypeToBeUpdated the ProductType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductType(ProductType productTypeToBeUpdated) {

		UpdateProductType com = new UpdateProductType(productTypeToBeUpdated);

		int usedTicketId;

		synchronized (ProductTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductTypeUpdated.class,
				event -> sendProductTypeChangedMessage(((ProductTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductType from the database
	 * 
	 * @param productTypeId:
	 *            the id of the ProductType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductTypeById(@RequestParam(value = "productTypeId") String productTypeId) {

		DeleteProductType com = new DeleteProductType(productTypeId);

		int usedTicketId;

		synchronized (ProductTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductTypeDeleted.class,
				event -> sendProductTypeChangedMessage(((ProductTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productType/\" plus one of the following: "
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

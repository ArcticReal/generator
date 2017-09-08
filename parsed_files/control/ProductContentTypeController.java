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
import com.skytala.eCommerce.command.AddProductContentType;
import com.skytala.eCommerce.command.DeleteProductContentType;
import com.skytala.eCommerce.command.UpdateProductContentType;
import com.skytala.eCommerce.entity.ProductContentType;
import com.skytala.eCommerce.entity.ProductContentTypeMapper;
import com.skytala.eCommerce.event.ProductContentTypeAdded;
import com.skytala.eCommerce.event.ProductContentTypeDeleted;
import com.skytala.eCommerce.event.ProductContentTypeFound;
import com.skytala.eCommerce.event.ProductContentTypeUpdated;
import com.skytala.eCommerce.query.FindProductContentTypesBy;

@RestController
@RequestMapping("/api/productContentType")
public class ProductContentTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductContentType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductContentTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductContentType
	 * @return a List with the ProductContentTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductContentType> findProductContentTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductContentTypesBy query = new FindProductContentTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductContentTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductContentTypeFound.class,
				event -> sendProductContentTypesFoundMessage(((ProductContentTypeFound) event).getProductContentTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductContentTypesFoundMessage(List<ProductContentType> productContentTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productContentTypes);
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
	public boolean createProductContentType(HttpServletRequest request) {

		ProductContentType productContentTypeToBeAdded = new ProductContentType();
		try {
			productContentTypeToBeAdded = ProductContentTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductContentType(productContentTypeToBeAdded);

	}

	/**
	 * creates a new ProductContentType entry in the ofbiz database
	 * 
	 * @param productContentTypeToBeAdded
	 *            the ProductContentType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductContentType(ProductContentType productContentTypeToBeAdded) {

		AddProductContentType com = new AddProductContentType(productContentTypeToBeAdded);
		int usedTicketId;

		synchronized (ProductContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductContentTypeAdded.class,
				event -> sendProductContentTypeChangedMessage(((ProductContentTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductContentType(HttpServletRequest request) {

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

		ProductContentType productContentTypeToBeUpdated = new ProductContentType();

		try {
			productContentTypeToBeUpdated = ProductContentTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductContentType(productContentTypeToBeUpdated);

	}

	/**
	 * Updates the ProductContentType with the specific Id
	 * 
	 * @param productContentTypeToBeUpdated the ProductContentType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductContentType(ProductContentType productContentTypeToBeUpdated) {

		UpdateProductContentType com = new UpdateProductContentType(productContentTypeToBeUpdated);

		int usedTicketId;

		synchronized (ProductContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductContentTypeUpdated.class,
				event -> sendProductContentTypeChangedMessage(((ProductContentTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductContentType from the database
	 * 
	 * @param productContentTypeId:
	 *            the id of the ProductContentType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductContentTypeById(@RequestParam(value = "productContentTypeId") String productContentTypeId) {

		DeleteProductContentType com = new DeleteProductContentType(productContentTypeId);

		int usedTicketId;

		synchronized (ProductContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductContentTypeDeleted.class,
				event -> sendProductContentTypeChangedMessage(((ProductContentTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductContentTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productContentType/\" plus one of the following: "
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

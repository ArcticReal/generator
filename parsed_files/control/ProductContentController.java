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
import com.skytala.eCommerce.command.AddProductContent;
import com.skytala.eCommerce.command.DeleteProductContent;
import com.skytala.eCommerce.command.UpdateProductContent;
import com.skytala.eCommerce.entity.ProductContent;
import com.skytala.eCommerce.entity.ProductContentMapper;
import com.skytala.eCommerce.event.ProductContentAdded;
import com.skytala.eCommerce.event.ProductContentDeleted;
import com.skytala.eCommerce.event.ProductContentFound;
import com.skytala.eCommerce.event.ProductContentUpdated;
import com.skytala.eCommerce.query.FindProductContentsBy;

@RestController
@RequestMapping("/api/productContent")
public class ProductContentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductContent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductContentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductContent
	 * @return a List with the ProductContents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductContent> findProductContentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductContentsBy query = new FindProductContentsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductContentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductContentFound.class,
				event -> sendProductContentsFoundMessage(((ProductContentFound) event).getProductContents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductContentsFoundMessage(List<ProductContent> productContents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productContents);
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
	public boolean createProductContent(HttpServletRequest request) {

		ProductContent productContentToBeAdded = new ProductContent();
		try {
			productContentToBeAdded = ProductContentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductContent(productContentToBeAdded);

	}

	/**
	 * creates a new ProductContent entry in the ofbiz database
	 * 
	 * @param productContentToBeAdded
	 *            the ProductContent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductContent(ProductContent productContentToBeAdded) {

		AddProductContent com = new AddProductContent(productContentToBeAdded);
		int usedTicketId;

		synchronized (ProductContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductContentAdded.class,
				event -> sendProductContentChangedMessage(((ProductContentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductContent(HttpServletRequest request) {

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

		ProductContent productContentToBeUpdated = new ProductContent();

		try {
			productContentToBeUpdated = ProductContentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductContent(productContentToBeUpdated);

	}

	/**
	 * Updates the ProductContent with the specific Id
	 * 
	 * @param productContentToBeUpdated the ProductContent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductContent(ProductContent productContentToBeUpdated) {

		UpdateProductContent com = new UpdateProductContent(productContentToBeUpdated);

		int usedTicketId;

		synchronized (ProductContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductContentUpdated.class,
				event -> sendProductContentChangedMessage(((ProductContentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductContent from the database
	 * 
	 * @param productContentId:
	 *            the id of the ProductContent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductContentById(@RequestParam(value = "productContentId") String productContentId) {

		DeleteProductContent com = new DeleteProductContent(productContentId);

		int usedTicketId;

		synchronized (ProductContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductContentDeleted.class,
				event -> sendProductContentChangedMessage(((ProductContentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductContentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productContent/\" plus one of the following: "
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

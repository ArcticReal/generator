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
import com.skytala.eCommerce.command.AddProductKeyword;
import com.skytala.eCommerce.command.DeleteProductKeyword;
import com.skytala.eCommerce.command.UpdateProductKeyword;
import com.skytala.eCommerce.entity.ProductKeyword;
import com.skytala.eCommerce.entity.ProductKeywordMapper;
import com.skytala.eCommerce.event.ProductKeywordAdded;
import com.skytala.eCommerce.event.ProductKeywordDeleted;
import com.skytala.eCommerce.event.ProductKeywordFound;
import com.skytala.eCommerce.event.ProductKeywordUpdated;
import com.skytala.eCommerce.query.FindProductKeywordsBy;

@RestController
@RequestMapping("/api/productKeyword")
public class ProductKeywordController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductKeyword>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductKeywordController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductKeyword
	 * @return a List with the ProductKeywords
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductKeyword> findProductKeywordsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductKeywordsBy query = new FindProductKeywordsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductKeywordController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductKeywordFound.class,
				event -> sendProductKeywordsFoundMessage(((ProductKeywordFound) event).getProductKeywords(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductKeywordsFoundMessage(List<ProductKeyword> productKeywords, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productKeywords);
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
	public boolean createProductKeyword(HttpServletRequest request) {

		ProductKeyword productKeywordToBeAdded = new ProductKeyword();
		try {
			productKeywordToBeAdded = ProductKeywordMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductKeyword(productKeywordToBeAdded);

	}

	/**
	 * creates a new ProductKeyword entry in the ofbiz database
	 * 
	 * @param productKeywordToBeAdded
	 *            the ProductKeyword thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductKeyword(ProductKeyword productKeywordToBeAdded) {

		AddProductKeyword com = new AddProductKeyword(productKeywordToBeAdded);
		int usedTicketId;

		synchronized (ProductKeywordController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductKeywordAdded.class,
				event -> sendProductKeywordChangedMessage(((ProductKeywordAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductKeyword(HttpServletRequest request) {

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

		ProductKeyword productKeywordToBeUpdated = new ProductKeyword();

		try {
			productKeywordToBeUpdated = ProductKeywordMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductKeyword(productKeywordToBeUpdated);

	}

	/**
	 * Updates the ProductKeyword with the specific Id
	 * 
	 * @param productKeywordToBeUpdated the ProductKeyword thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductKeyword(ProductKeyword productKeywordToBeUpdated) {

		UpdateProductKeyword com = new UpdateProductKeyword(productKeywordToBeUpdated);

		int usedTicketId;

		synchronized (ProductKeywordController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductKeywordUpdated.class,
				event -> sendProductKeywordChangedMessage(((ProductKeywordUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductKeyword from the database
	 * 
	 * @param productKeywordId:
	 *            the id of the ProductKeyword thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductKeywordById(@RequestParam(value = "productKeywordId") String productKeywordId) {

		DeleteProductKeyword com = new DeleteProductKeyword(productKeywordId);

		int usedTicketId;

		synchronized (ProductKeywordController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductKeywordDeleted.class,
				event -> sendProductKeywordChangedMessage(((ProductKeywordDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductKeywordChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productKeyword/\" plus one of the following: "
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

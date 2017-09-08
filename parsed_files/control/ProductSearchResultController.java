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
import com.skytala.eCommerce.command.AddProductSearchResult;
import com.skytala.eCommerce.command.DeleteProductSearchResult;
import com.skytala.eCommerce.command.UpdateProductSearchResult;
import com.skytala.eCommerce.entity.ProductSearchResult;
import com.skytala.eCommerce.entity.ProductSearchResultMapper;
import com.skytala.eCommerce.event.ProductSearchResultAdded;
import com.skytala.eCommerce.event.ProductSearchResultDeleted;
import com.skytala.eCommerce.event.ProductSearchResultFound;
import com.skytala.eCommerce.event.ProductSearchResultUpdated;
import com.skytala.eCommerce.query.FindProductSearchResultsBy;

@RestController
@RequestMapping("/api/productSearchResult")
public class ProductSearchResultController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductSearchResult>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductSearchResultController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductSearchResult
	 * @return a List with the ProductSearchResults
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductSearchResult> findProductSearchResultsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductSearchResultsBy query = new FindProductSearchResultsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductSearchResultController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductSearchResultFound.class,
				event -> sendProductSearchResultsFoundMessage(((ProductSearchResultFound) event).getProductSearchResults(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductSearchResultsFoundMessage(List<ProductSearchResult> productSearchResults, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productSearchResults);
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
	public boolean createProductSearchResult(HttpServletRequest request) {

		ProductSearchResult productSearchResultToBeAdded = new ProductSearchResult();
		try {
			productSearchResultToBeAdded = ProductSearchResultMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductSearchResult(productSearchResultToBeAdded);

	}

	/**
	 * creates a new ProductSearchResult entry in the ofbiz database
	 * 
	 * @param productSearchResultToBeAdded
	 *            the ProductSearchResult thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductSearchResult(ProductSearchResult productSearchResultToBeAdded) {

		AddProductSearchResult com = new AddProductSearchResult(productSearchResultToBeAdded);
		int usedTicketId;

		synchronized (ProductSearchResultController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductSearchResultAdded.class,
				event -> sendProductSearchResultChangedMessage(((ProductSearchResultAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductSearchResult(HttpServletRequest request) {

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

		ProductSearchResult productSearchResultToBeUpdated = new ProductSearchResult();

		try {
			productSearchResultToBeUpdated = ProductSearchResultMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductSearchResult(productSearchResultToBeUpdated);

	}

	/**
	 * Updates the ProductSearchResult with the specific Id
	 * 
	 * @param productSearchResultToBeUpdated the ProductSearchResult thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductSearchResult(ProductSearchResult productSearchResultToBeUpdated) {

		UpdateProductSearchResult com = new UpdateProductSearchResult(productSearchResultToBeUpdated);

		int usedTicketId;

		synchronized (ProductSearchResultController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductSearchResultUpdated.class,
				event -> sendProductSearchResultChangedMessage(((ProductSearchResultUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductSearchResult from the database
	 * 
	 * @param productSearchResultId:
	 *            the id of the ProductSearchResult thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductSearchResultById(@RequestParam(value = "productSearchResultId") String productSearchResultId) {

		DeleteProductSearchResult com = new DeleteProductSearchResult(productSearchResultId);

		int usedTicketId;

		synchronized (ProductSearchResultController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductSearchResultDeleted.class,
				event -> sendProductSearchResultChangedMessage(((ProductSearchResultDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductSearchResultChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productSearchResult/\" plus one of the following: "
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

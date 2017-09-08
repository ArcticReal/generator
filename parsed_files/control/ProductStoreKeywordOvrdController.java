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
import com.skytala.eCommerce.command.AddProductStoreKeywordOvrd;
import com.skytala.eCommerce.command.DeleteProductStoreKeywordOvrd;
import com.skytala.eCommerce.command.UpdateProductStoreKeywordOvrd;
import com.skytala.eCommerce.entity.ProductStoreKeywordOvrd;
import com.skytala.eCommerce.entity.ProductStoreKeywordOvrdMapper;
import com.skytala.eCommerce.event.ProductStoreKeywordOvrdAdded;
import com.skytala.eCommerce.event.ProductStoreKeywordOvrdDeleted;
import com.skytala.eCommerce.event.ProductStoreKeywordOvrdFound;
import com.skytala.eCommerce.event.ProductStoreKeywordOvrdUpdated;
import com.skytala.eCommerce.query.FindProductStoreKeywordOvrdsBy;

@RestController
@RequestMapping("/api/productStoreKeywordOvrd")
public class ProductStoreKeywordOvrdController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductStoreKeywordOvrd>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductStoreKeywordOvrdController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductStoreKeywordOvrd
	 * @return a List with the ProductStoreKeywordOvrds
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductStoreKeywordOvrd> findProductStoreKeywordOvrdsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductStoreKeywordOvrdsBy query = new FindProductStoreKeywordOvrdsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductStoreKeywordOvrdController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreKeywordOvrdFound.class,
				event -> sendProductStoreKeywordOvrdsFoundMessage(((ProductStoreKeywordOvrdFound) event).getProductStoreKeywordOvrds(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductStoreKeywordOvrdsFoundMessage(List<ProductStoreKeywordOvrd> productStoreKeywordOvrds, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productStoreKeywordOvrds);
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
	public boolean createProductStoreKeywordOvrd(HttpServletRequest request) {

		ProductStoreKeywordOvrd productStoreKeywordOvrdToBeAdded = new ProductStoreKeywordOvrd();
		try {
			productStoreKeywordOvrdToBeAdded = ProductStoreKeywordOvrdMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductStoreKeywordOvrd(productStoreKeywordOvrdToBeAdded);

	}

	/**
	 * creates a new ProductStoreKeywordOvrd entry in the ofbiz database
	 * 
	 * @param productStoreKeywordOvrdToBeAdded
	 *            the ProductStoreKeywordOvrd thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductStoreKeywordOvrd(ProductStoreKeywordOvrd productStoreKeywordOvrdToBeAdded) {

		AddProductStoreKeywordOvrd com = new AddProductStoreKeywordOvrd(productStoreKeywordOvrdToBeAdded);
		int usedTicketId;

		synchronized (ProductStoreKeywordOvrdController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreKeywordOvrdAdded.class,
				event -> sendProductStoreKeywordOvrdChangedMessage(((ProductStoreKeywordOvrdAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductStoreKeywordOvrd(HttpServletRequest request) {

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

		ProductStoreKeywordOvrd productStoreKeywordOvrdToBeUpdated = new ProductStoreKeywordOvrd();

		try {
			productStoreKeywordOvrdToBeUpdated = ProductStoreKeywordOvrdMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductStoreKeywordOvrd(productStoreKeywordOvrdToBeUpdated);

	}

	/**
	 * Updates the ProductStoreKeywordOvrd with the specific Id
	 * 
	 * @param productStoreKeywordOvrdToBeUpdated the ProductStoreKeywordOvrd thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductStoreKeywordOvrd(ProductStoreKeywordOvrd productStoreKeywordOvrdToBeUpdated) {

		UpdateProductStoreKeywordOvrd com = new UpdateProductStoreKeywordOvrd(productStoreKeywordOvrdToBeUpdated);

		int usedTicketId;

		synchronized (ProductStoreKeywordOvrdController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreKeywordOvrdUpdated.class,
				event -> sendProductStoreKeywordOvrdChangedMessage(((ProductStoreKeywordOvrdUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductStoreKeywordOvrd from the database
	 * 
	 * @param productStoreKeywordOvrdId:
	 *            the id of the ProductStoreKeywordOvrd thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductStoreKeywordOvrdById(@RequestParam(value = "productStoreKeywordOvrdId") String productStoreKeywordOvrdId) {

		DeleteProductStoreKeywordOvrd com = new DeleteProductStoreKeywordOvrd(productStoreKeywordOvrdId);

		int usedTicketId;

		synchronized (ProductStoreKeywordOvrdController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreKeywordOvrdDeleted.class,
				event -> sendProductStoreKeywordOvrdChangedMessage(((ProductStoreKeywordOvrdDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductStoreKeywordOvrdChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productStoreKeywordOvrd/\" plus one of the following: "
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

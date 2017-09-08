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
import com.skytala.eCommerce.command.AddProductConfigOptionIactn;
import com.skytala.eCommerce.command.DeleteProductConfigOptionIactn;
import com.skytala.eCommerce.command.UpdateProductConfigOptionIactn;
import com.skytala.eCommerce.entity.ProductConfigOptionIactn;
import com.skytala.eCommerce.entity.ProductConfigOptionIactnMapper;
import com.skytala.eCommerce.event.ProductConfigOptionIactnAdded;
import com.skytala.eCommerce.event.ProductConfigOptionIactnDeleted;
import com.skytala.eCommerce.event.ProductConfigOptionIactnFound;
import com.skytala.eCommerce.event.ProductConfigOptionIactnUpdated;
import com.skytala.eCommerce.query.FindProductConfigOptionIactnsBy;

@RestController
@RequestMapping("/api/productConfigOptionIactn")
public class ProductConfigOptionIactnController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductConfigOptionIactn>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductConfigOptionIactnController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductConfigOptionIactn
	 * @return a List with the ProductConfigOptionIactns
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductConfigOptionIactn> findProductConfigOptionIactnsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductConfigOptionIactnsBy query = new FindProductConfigOptionIactnsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductConfigOptionIactnController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigOptionIactnFound.class,
				event -> sendProductConfigOptionIactnsFoundMessage(((ProductConfigOptionIactnFound) event).getProductConfigOptionIactns(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductConfigOptionIactnsFoundMessage(List<ProductConfigOptionIactn> productConfigOptionIactns, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productConfigOptionIactns);
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
	public boolean createProductConfigOptionIactn(HttpServletRequest request) {

		ProductConfigOptionIactn productConfigOptionIactnToBeAdded = new ProductConfigOptionIactn();
		try {
			productConfigOptionIactnToBeAdded = ProductConfigOptionIactnMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductConfigOptionIactn(productConfigOptionIactnToBeAdded);

	}

	/**
	 * creates a new ProductConfigOptionIactn entry in the ofbiz database
	 * 
	 * @param productConfigOptionIactnToBeAdded
	 *            the ProductConfigOptionIactn thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductConfigOptionIactn(ProductConfigOptionIactn productConfigOptionIactnToBeAdded) {

		AddProductConfigOptionIactn com = new AddProductConfigOptionIactn(productConfigOptionIactnToBeAdded);
		int usedTicketId;

		synchronized (ProductConfigOptionIactnController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigOptionIactnAdded.class,
				event -> sendProductConfigOptionIactnChangedMessage(((ProductConfigOptionIactnAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductConfigOptionIactn(HttpServletRequest request) {

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

		ProductConfigOptionIactn productConfigOptionIactnToBeUpdated = new ProductConfigOptionIactn();

		try {
			productConfigOptionIactnToBeUpdated = ProductConfigOptionIactnMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductConfigOptionIactn(productConfigOptionIactnToBeUpdated);

	}

	/**
	 * Updates the ProductConfigOptionIactn with the specific Id
	 * 
	 * @param productConfigOptionIactnToBeUpdated the ProductConfigOptionIactn thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductConfigOptionIactn(ProductConfigOptionIactn productConfigOptionIactnToBeUpdated) {

		UpdateProductConfigOptionIactn com = new UpdateProductConfigOptionIactn(productConfigOptionIactnToBeUpdated);

		int usedTicketId;

		synchronized (ProductConfigOptionIactnController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigOptionIactnUpdated.class,
				event -> sendProductConfigOptionIactnChangedMessage(((ProductConfigOptionIactnUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductConfigOptionIactn from the database
	 * 
	 * @param productConfigOptionIactnId:
	 *            the id of the ProductConfigOptionIactn thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductConfigOptionIactnById(@RequestParam(value = "productConfigOptionIactnId") String productConfigOptionIactnId) {

		DeleteProductConfigOptionIactn com = new DeleteProductConfigOptionIactn(productConfigOptionIactnId);

		int usedTicketId;

		synchronized (ProductConfigOptionIactnController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductConfigOptionIactnDeleted.class,
				event -> sendProductConfigOptionIactnChangedMessage(((ProductConfigOptionIactnDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductConfigOptionIactnChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productConfigOptionIactn/\" plus one of the following: "
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

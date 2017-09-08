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
import com.skytala.eCommerce.command.AddProductFeatureIactn;
import com.skytala.eCommerce.command.DeleteProductFeatureIactn;
import com.skytala.eCommerce.command.UpdateProductFeatureIactn;
import com.skytala.eCommerce.entity.ProductFeatureIactn;
import com.skytala.eCommerce.entity.ProductFeatureIactnMapper;
import com.skytala.eCommerce.event.ProductFeatureIactnAdded;
import com.skytala.eCommerce.event.ProductFeatureIactnDeleted;
import com.skytala.eCommerce.event.ProductFeatureIactnFound;
import com.skytala.eCommerce.event.ProductFeatureIactnUpdated;
import com.skytala.eCommerce.query.FindProductFeatureIactnsBy;

@RestController
@RequestMapping("/api/productFeatureIactn")
public class ProductFeatureIactnController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductFeatureIactn>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductFeatureIactnController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductFeatureIactn
	 * @return a List with the ProductFeatureIactns
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductFeatureIactn> findProductFeatureIactnsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductFeatureIactnsBy query = new FindProductFeatureIactnsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductFeatureIactnController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureIactnFound.class,
				event -> sendProductFeatureIactnsFoundMessage(((ProductFeatureIactnFound) event).getProductFeatureIactns(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductFeatureIactnsFoundMessage(List<ProductFeatureIactn> productFeatureIactns, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productFeatureIactns);
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
	public boolean createProductFeatureIactn(HttpServletRequest request) {

		ProductFeatureIactn productFeatureIactnToBeAdded = new ProductFeatureIactn();
		try {
			productFeatureIactnToBeAdded = ProductFeatureIactnMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductFeatureIactn(productFeatureIactnToBeAdded);

	}

	/**
	 * creates a new ProductFeatureIactn entry in the ofbiz database
	 * 
	 * @param productFeatureIactnToBeAdded
	 *            the ProductFeatureIactn thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductFeatureIactn(ProductFeatureIactn productFeatureIactnToBeAdded) {

		AddProductFeatureIactn com = new AddProductFeatureIactn(productFeatureIactnToBeAdded);
		int usedTicketId;

		synchronized (ProductFeatureIactnController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureIactnAdded.class,
				event -> sendProductFeatureIactnChangedMessage(((ProductFeatureIactnAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductFeatureIactn(HttpServletRequest request) {

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

		ProductFeatureIactn productFeatureIactnToBeUpdated = new ProductFeatureIactn();

		try {
			productFeatureIactnToBeUpdated = ProductFeatureIactnMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductFeatureIactn(productFeatureIactnToBeUpdated);

	}

	/**
	 * Updates the ProductFeatureIactn with the specific Id
	 * 
	 * @param productFeatureIactnToBeUpdated the ProductFeatureIactn thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductFeatureIactn(ProductFeatureIactn productFeatureIactnToBeUpdated) {

		UpdateProductFeatureIactn com = new UpdateProductFeatureIactn(productFeatureIactnToBeUpdated);

		int usedTicketId;

		synchronized (ProductFeatureIactnController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureIactnUpdated.class,
				event -> sendProductFeatureIactnChangedMessage(((ProductFeatureIactnUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductFeatureIactn from the database
	 * 
	 * @param productFeatureIactnId:
	 *            the id of the ProductFeatureIactn thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductFeatureIactnById(@RequestParam(value = "productFeatureIactnId") String productFeatureIactnId) {

		DeleteProductFeatureIactn com = new DeleteProductFeatureIactn(productFeatureIactnId);

		int usedTicketId;

		synchronized (ProductFeatureIactnController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureIactnDeleted.class,
				event -> sendProductFeatureIactnChangedMessage(((ProductFeatureIactnDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductFeatureIactnChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productFeatureIactn/\" plus one of the following: "
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

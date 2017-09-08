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
import com.skytala.eCommerce.command.AddProductCategoryRollup;
import com.skytala.eCommerce.command.DeleteProductCategoryRollup;
import com.skytala.eCommerce.command.UpdateProductCategoryRollup;
import com.skytala.eCommerce.entity.ProductCategoryRollup;
import com.skytala.eCommerce.entity.ProductCategoryRollupMapper;
import com.skytala.eCommerce.event.ProductCategoryRollupAdded;
import com.skytala.eCommerce.event.ProductCategoryRollupDeleted;
import com.skytala.eCommerce.event.ProductCategoryRollupFound;
import com.skytala.eCommerce.event.ProductCategoryRollupUpdated;
import com.skytala.eCommerce.query.FindProductCategoryRollupsBy;

@RestController
@RequestMapping("/api/productCategoryRollup")
public class ProductCategoryRollupController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductCategoryRollup>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductCategoryRollupController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductCategoryRollup
	 * @return a List with the ProductCategoryRollups
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductCategoryRollup> findProductCategoryRollupsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductCategoryRollupsBy query = new FindProductCategoryRollupsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductCategoryRollupController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryRollupFound.class,
				event -> sendProductCategoryRollupsFoundMessage(((ProductCategoryRollupFound) event).getProductCategoryRollups(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductCategoryRollupsFoundMessage(List<ProductCategoryRollup> productCategoryRollups, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productCategoryRollups);
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
	public boolean createProductCategoryRollup(HttpServletRequest request) {

		ProductCategoryRollup productCategoryRollupToBeAdded = new ProductCategoryRollup();
		try {
			productCategoryRollupToBeAdded = ProductCategoryRollupMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductCategoryRollup(productCategoryRollupToBeAdded);

	}

	/**
	 * creates a new ProductCategoryRollup entry in the ofbiz database
	 * 
	 * @param productCategoryRollupToBeAdded
	 *            the ProductCategoryRollup thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductCategoryRollup(ProductCategoryRollup productCategoryRollupToBeAdded) {

		AddProductCategoryRollup com = new AddProductCategoryRollup(productCategoryRollupToBeAdded);
		int usedTicketId;

		synchronized (ProductCategoryRollupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryRollupAdded.class,
				event -> sendProductCategoryRollupChangedMessage(((ProductCategoryRollupAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductCategoryRollup(HttpServletRequest request) {

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

		ProductCategoryRollup productCategoryRollupToBeUpdated = new ProductCategoryRollup();

		try {
			productCategoryRollupToBeUpdated = ProductCategoryRollupMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductCategoryRollup(productCategoryRollupToBeUpdated);

	}

	/**
	 * Updates the ProductCategoryRollup with the specific Id
	 * 
	 * @param productCategoryRollupToBeUpdated the ProductCategoryRollup thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductCategoryRollup(ProductCategoryRollup productCategoryRollupToBeUpdated) {

		UpdateProductCategoryRollup com = new UpdateProductCategoryRollup(productCategoryRollupToBeUpdated);

		int usedTicketId;

		synchronized (ProductCategoryRollupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryRollupUpdated.class,
				event -> sendProductCategoryRollupChangedMessage(((ProductCategoryRollupUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductCategoryRollup from the database
	 * 
	 * @param productCategoryRollupId:
	 *            the id of the ProductCategoryRollup thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductCategoryRollupById(@RequestParam(value = "productCategoryRollupId") String productCategoryRollupId) {

		DeleteProductCategoryRollup com = new DeleteProductCategoryRollup(productCategoryRollupId);

		int usedTicketId;

		synchronized (ProductCategoryRollupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryRollupDeleted.class,
				event -> sendProductCategoryRollupChangedMessage(((ProductCategoryRollupDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductCategoryRollupChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productCategoryRollup/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddProductFeatureCategory;
import com.skytala.eCommerce.command.DeleteProductFeatureCategory;
import com.skytala.eCommerce.command.UpdateProductFeatureCategory;
import com.skytala.eCommerce.entity.ProductFeatureCategory;
import com.skytala.eCommerce.entity.ProductFeatureCategoryMapper;
import com.skytala.eCommerce.event.ProductFeatureCategoryAdded;
import com.skytala.eCommerce.event.ProductFeatureCategoryDeleted;
import com.skytala.eCommerce.event.ProductFeatureCategoryFound;
import com.skytala.eCommerce.event.ProductFeatureCategoryUpdated;
import com.skytala.eCommerce.query.FindProductFeatureCategorysBy;

@RestController
@RequestMapping("/api/productFeatureCategory")
public class ProductFeatureCategoryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductFeatureCategory>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductFeatureCategoryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductFeatureCategory
	 * @return a List with the ProductFeatureCategorys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductFeatureCategory> findProductFeatureCategorysBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductFeatureCategorysBy query = new FindProductFeatureCategorysBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductFeatureCategoryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureCategoryFound.class,
				event -> sendProductFeatureCategorysFoundMessage(((ProductFeatureCategoryFound) event).getProductFeatureCategorys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductFeatureCategorysFoundMessage(List<ProductFeatureCategory> productFeatureCategorys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productFeatureCategorys);
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
	public boolean createProductFeatureCategory(HttpServletRequest request) {

		ProductFeatureCategory productFeatureCategoryToBeAdded = new ProductFeatureCategory();
		try {
			productFeatureCategoryToBeAdded = ProductFeatureCategoryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductFeatureCategory(productFeatureCategoryToBeAdded);

	}

	/**
	 * creates a new ProductFeatureCategory entry in the ofbiz database
	 * 
	 * @param productFeatureCategoryToBeAdded
	 *            the ProductFeatureCategory thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductFeatureCategory(ProductFeatureCategory productFeatureCategoryToBeAdded) {

		AddProductFeatureCategory com = new AddProductFeatureCategory(productFeatureCategoryToBeAdded);
		int usedTicketId;

		synchronized (ProductFeatureCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureCategoryAdded.class,
				event -> sendProductFeatureCategoryChangedMessage(((ProductFeatureCategoryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductFeatureCategory(HttpServletRequest request) {

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

		ProductFeatureCategory productFeatureCategoryToBeUpdated = new ProductFeatureCategory();

		try {
			productFeatureCategoryToBeUpdated = ProductFeatureCategoryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductFeatureCategory(productFeatureCategoryToBeUpdated);

	}

	/**
	 * Updates the ProductFeatureCategory with the specific Id
	 * 
	 * @param productFeatureCategoryToBeUpdated the ProductFeatureCategory thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductFeatureCategory(ProductFeatureCategory productFeatureCategoryToBeUpdated) {

		UpdateProductFeatureCategory com = new UpdateProductFeatureCategory(productFeatureCategoryToBeUpdated);

		int usedTicketId;

		synchronized (ProductFeatureCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureCategoryUpdated.class,
				event -> sendProductFeatureCategoryChangedMessage(((ProductFeatureCategoryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductFeatureCategory from the database
	 * 
	 * @param productFeatureCategoryId:
	 *            the id of the ProductFeatureCategory thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductFeatureCategoryById(@RequestParam(value = "productFeatureCategoryId") String productFeatureCategoryId) {

		DeleteProductFeatureCategory com = new DeleteProductFeatureCategory(productFeatureCategoryId);

		int usedTicketId;

		synchronized (ProductFeatureCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureCategoryDeleted.class,
				event -> sendProductFeatureCategoryChangedMessage(((ProductFeatureCategoryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductFeatureCategoryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productFeatureCategory/\" plus one of the following: "
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

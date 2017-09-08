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
import com.skytala.eCommerce.command.AddProductCategory;
import com.skytala.eCommerce.command.DeleteProductCategory;
import com.skytala.eCommerce.command.UpdateProductCategory;
import com.skytala.eCommerce.entity.ProductCategory;
import com.skytala.eCommerce.entity.ProductCategoryMapper;
import com.skytala.eCommerce.event.ProductCategoryAdded;
import com.skytala.eCommerce.event.ProductCategoryDeleted;
import com.skytala.eCommerce.event.ProductCategoryFound;
import com.skytala.eCommerce.event.ProductCategoryUpdated;
import com.skytala.eCommerce.query.FindProductCategorysBy;

@RestController
@RequestMapping("/api/productCategory")
public class ProductCategoryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductCategory>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductCategoryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductCategory
	 * @return a List with the ProductCategorys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductCategory> findProductCategorysBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductCategorysBy query = new FindProductCategorysBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductCategoryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryFound.class,
				event -> sendProductCategorysFoundMessage(((ProductCategoryFound) event).getProductCategorys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductCategorysFoundMessage(List<ProductCategory> productCategorys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productCategorys);
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
	public boolean createProductCategory(HttpServletRequest request) {

		ProductCategory productCategoryToBeAdded = new ProductCategory();
		try {
			productCategoryToBeAdded = ProductCategoryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductCategory(productCategoryToBeAdded);

	}

	/**
	 * creates a new ProductCategory entry in the ofbiz database
	 * 
	 * @param productCategoryToBeAdded
	 *            the ProductCategory thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductCategory(ProductCategory productCategoryToBeAdded) {

		AddProductCategory com = new AddProductCategory(productCategoryToBeAdded);
		int usedTicketId;

		synchronized (ProductCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryAdded.class,
				event -> sendProductCategoryChangedMessage(((ProductCategoryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductCategory(HttpServletRequest request) {

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

		ProductCategory productCategoryToBeUpdated = new ProductCategory();

		try {
			productCategoryToBeUpdated = ProductCategoryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductCategory(productCategoryToBeUpdated);

	}

	/**
	 * Updates the ProductCategory with the specific Id
	 * 
	 * @param productCategoryToBeUpdated the ProductCategory thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductCategory(ProductCategory productCategoryToBeUpdated) {

		UpdateProductCategory com = new UpdateProductCategory(productCategoryToBeUpdated);

		int usedTicketId;

		synchronized (ProductCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryUpdated.class,
				event -> sendProductCategoryChangedMessage(((ProductCategoryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductCategory from the database
	 * 
	 * @param productCategoryId:
	 *            the id of the ProductCategory thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductCategoryById(@RequestParam(value = "productCategoryId") String productCategoryId) {

		DeleteProductCategory com = new DeleteProductCategory(productCategoryId);

		int usedTicketId;

		synchronized (ProductCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryDeleted.class,
				event -> sendProductCategoryChangedMessage(((ProductCategoryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductCategoryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productCategory/\" plus one of the following: "
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

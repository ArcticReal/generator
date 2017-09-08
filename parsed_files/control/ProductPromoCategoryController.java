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
import com.skytala.eCommerce.command.AddProductPromoCategory;
import com.skytala.eCommerce.command.DeleteProductPromoCategory;
import com.skytala.eCommerce.command.UpdateProductPromoCategory;
import com.skytala.eCommerce.entity.ProductPromoCategory;
import com.skytala.eCommerce.entity.ProductPromoCategoryMapper;
import com.skytala.eCommerce.event.ProductPromoCategoryAdded;
import com.skytala.eCommerce.event.ProductPromoCategoryDeleted;
import com.skytala.eCommerce.event.ProductPromoCategoryFound;
import com.skytala.eCommerce.event.ProductPromoCategoryUpdated;
import com.skytala.eCommerce.query.FindProductPromoCategorysBy;

@RestController
@RequestMapping("/api/productPromoCategory")
public class ProductPromoCategoryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPromoCategory>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPromoCategoryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPromoCategory
	 * @return a List with the ProductPromoCategorys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPromoCategory> findProductPromoCategorysBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPromoCategorysBy query = new FindProductPromoCategorysBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPromoCategoryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCategoryFound.class,
				event -> sendProductPromoCategorysFoundMessage(((ProductPromoCategoryFound) event).getProductPromoCategorys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPromoCategorysFoundMessage(List<ProductPromoCategory> productPromoCategorys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPromoCategorys);
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
	public boolean createProductPromoCategory(HttpServletRequest request) {

		ProductPromoCategory productPromoCategoryToBeAdded = new ProductPromoCategory();
		try {
			productPromoCategoryToBeAdded = ProductPromoCategoryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPromoCategory(productPromoCategoryToBeAdded);

	}

	/**
	 * creates a new ProductPromoCategory entry in the ofbiz database
	 * 
	 * @param productPromoCategoryToBeAdded
	 *            the ProductPromoCategory thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPromoCategory(ProductPromoCategory productPromoCategoryToBeAdded) {

		AddProductPromoCategory com = new AddProductPromoCategory(productPromoCategoryToBeAdded);
		int usedTicketId;

		synchronized (ProductPromoCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCategoryAdded.class,
				event -> sendProductPromoCategoryChangedMessage(((ProductPromoCategoryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPromoCategory(HttpServletRequest request) {

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

		ProductPromoCategory productPromoCategoryToBeUpdated = new ProductPromoCategory();

		try {
			productPromoCategoryToBeUpdated = ProductPromoCategoryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPromoCategory(productPromoCategoryToBeUpdated);

	}

	/**
	 * Updates the ProductPromoCategory with the specific Id
	 * 
	 * @param productPromoCategoryToBeUpdated the ProductPromoCategory thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPromoCategory(ProductPromoCategory productPromoCategoryToBeUpdated) {

		UpdateProductPromoCategory com = new UpdateProductPromoCategory(productPromoCategoryToBeUpdated);

		int usedTicketId;

		synchronized (ProductPromoCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCategoryUpdated.class,
				event -> sendProductPromoCategoryChangedMessage(((ProductPromoCategoryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPromoCategory from the database
	 * 
	 * @param productPromoCategoryId:
	 *            the id of the ProductPromoCategory thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPromoCategoryById(@RequestParam(value = "productPromoCategoryId") String productPromoCategoryId) {

		DeleteProductPromoCategory com = new DeleteProductPromoCategory(productPromoCategoryId);

		int usedTicketId;

		synchronized (ProductPromoCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCategoryDeleted.class,
				event -> sendProductPromoCategoryChangedMessage(((ProductPromoCategoryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPromoCategoryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPromoCategory/\" plus one of the following: "
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

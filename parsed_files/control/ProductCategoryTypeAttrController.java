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
import com.skytala.eCommerce.command.AddProductCategoryTypeAttr;
import com.skytala.eCommerce.command.DeleteProductCategoryTypeAttr;
import com.skytala.eCommerce.command.UpdateProductCategoryTypeAttr;
import com.skytala.eCommerce.entity.ProductCategoryTypeAttr;
import com.skytala.eCommerce.entity.ProductCategoryTypeAttrMapper;
import com.skytala.eCommerce.event.ProductCategoryTypeAttrAdded;
import com.skytala.eCommerce.event.ProductCategoryTypeAttrDeleted;
import com.skytala.eCommerce.event.ProductCategoryTypeAttrFound;
import com.skytala.eCommerce.event.ProductCategoryTypeAttrUpdated;
import com.skytala.eCommerce.query.FindProductCategoryTypeAttrsBy;

@RestController
@RequestMapping("/api/productCategoryTypeAttr")
public class ProductCategoryTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductCategoryTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductCategoryTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductCategoryTypeAttr
	 * @return a List with the ProductCategoryTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductCategoryTypeAttr> findProductCategoryTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductCategoryTypeAttrsBy query = new FindProductCategoryTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductCategoryTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryTypeAttrFound.class,
				event -> sendProductCategoryTypeAttrsFoundMessage(((ProductCategoryTypeAttrFound) event).getProductCategoryTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductCategoryTypeAttrsFoundMessage(List<ProductCategoryTypeAttr> productCategoryTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productCategoryTypeAttrs);
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
	public boolean createProductCategoryTypeAttr(HttpServletRequest request) {

		ProductCategoryTypeAttr productCategoryTypeAttrToBeAdded = new ProductCategoryTypeAttr();
		try {
			productCategoryTypeAttrToBeAdded = ProductCategoryTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductCategoryTypeAttr(productCategoryTypeAttrToBeAdded);

	}

	/**
	 * creates a new ProductCategoryTypeAttr entry in the ofbiz database
	 * 
	 * @param productCategoryTypeAttrToBeAdded
	 *            the ProductCategoryTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductCategoryTypeAttr(ProductCategoryTypeAttr productCategoryTypeAttrToBeAdded) {

		AddProductCategoryTypeAttr com = new AddProductCategoryTypeAttr(productCategoryTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (ProductCategoryTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryTypeAttrAdded.class,
				event -> sendProductCategoryTypeAttrChangedMessage(((ProductCategoryTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductCategoryTypeAttr(HttpServletRequest request) {

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

		ProductCategoryTypeAttr productCategoryTypeAttrToBeUpdated = new ProductCategoryTypeAttr();

		try {
			productCategoryTypeAttrToBeUpdated = ProductCategoryTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductCategoryTypeAttr(productCategoryTypeAttrToBeUpdated);

	}

	/**
	 * Updates the ProductCategoryTypeAttr with the specific Id
	 * 
	 * @param productCategoryTypeAttrToBeUpdated the ProductCategoryTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductCategoryTypeAttr(ProductCategoryTypeAttr productCategoryTypeAttrToBeUpdated) {

		UpdateProductCategoryTypeAttr com = new UpdateProductCategoryTypeAttr(productCategoryTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (ProductCategoryTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryTypeAttrUpdated.class,
				event -> sendProductCategoryTypeAttrChangedMessage(((ProductCategoryTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductCategoryTypeAttr from the database
	 * 
	 * @param productCategoryTypeAttrId:
	 *            the id of the ProductCategoryTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductCategoryTypeAttrById(@RequestParam(value = "productCategoryTypeAttrId") String productCategoryTypeAttrId) {

		DeleteProductCategoryTypeAttr com = new DeleteProductCategoryTypeAttr(productCategoryTypeAttrId);

		int usedTicketId;

		synchronized (ProductCategoryTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryTypeAttrDeleted.class,
				event -> sendProductCategoryTypeAttrChangedMessage(((ProductCategoryTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductCategoryTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productCategoryTypeAttr/\" plus one of the following: "
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

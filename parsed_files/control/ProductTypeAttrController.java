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
import com.skytala.eCommerce.command.AddProductTypeAttr;
import com.skytala.eCommerce.command.DeleteProductTypeAttr;
import com.skytala.eCommerce.command.UpdateProductTypeAttr;
import com.skytala.eCommerce.entity.ProductTypeAttr;
import com.skytala.eCommerce.entity.ProductTypeAttrMapper;
import com.skytala.eCommerce.event.ProductTypeAttrAdded;
import com.skytala.eCommerce.event.ProductTypeAttrDeleted;
import com.skytala.eCommerce.event.ProductTypeAttrFound;
import com.skytala.eCommerce.event.ProductTypeAttrUpdated;
import com.skytala.eCommerce.query.FindProductTypeAttrsBy;

@RestController
@RequestMapping("/api/productTypeAttr")
public class ProductTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductTypeAttr
	 * @return a List with the ProductTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductTypeAttr> findProductTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductTypeAttrsBy query = new FindProductTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductTypeAttrFound.class,
				event -> sendProductTypeAttrsFoundMessage(((ProductTypeAttrFound) event).getProductTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductTypeAttrsFoundMessage(List<ProductTypeAttr> productTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productTypeAttrs);
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
	public boolean createProductTypeAttr(HttpServletRequest request) {

		ProductTypeAttr productTypeAttrToBeAdded = new ProductTypeAttr();
		try {
			productTypeAttrToBeAdded = ProductTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductTypeAttr(productTypeAttrToBeAdded);

	}

	/**
	 * creates a new ProductTypeAttr entry in the ofbiz database
	 * 
	 * @param productTypeAttrToBeAdded
	 *            the ProductTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductTypeAttr(ProductTypeAttr productTypeAttrToBeAdded) {

		AddProductTypeAttr com = new AddProductTypeAttr(productTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (ProductTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductTypeAttrAdded.class,
				event -> sendProductTypeAttrChangedMessage(((ProductTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductTypeAttr(HttpServletRequest request) {

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

		ProductTypeAttr productTypeAttrToBeUpdated = new ProductTypeAttr();

		try {
			productTypeAttrToBeUpdated = ProductTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductTypeAttr(productTypeAttrToBeUpdated);

	}

	/**
	 * Updates the ProductTypeAttr with the specific Id
	 * 
	 * @param productTypeAttrToBeUpdated the ProductTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductTypeAttr(ProductTypeAttr productTypeAttrToBeUpdated) {

		UpdateProductTypeAttr com = new UpdateProductTypeAttr(productTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (ProductTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductTypeAttrUpdated.class,
				event -> sendProductTypeAttrChangedMessage(((ProductTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductTypeAttr from the database
	 * 
	 * @param productTypeAttrId:
	 *            the id of the ProductTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductTypeAttrById(@RequestParam(value = "productTypeAttrId") String productTypeAttrId) {

		DeleteProductTypeAttr com = new DeleteProductTypeAttr(productTypeAttrId);

		int usedTicketId;

		synchronized (ProductTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductTypeAttrDeleted.class,
				event -> sendProductTypeAttrChangedMessage(((ProductTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productTypeAttr/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddProductFeatureApplAttr;
import com.skytala.eCommerce.command.DeleteProductFeatureApplAttr;
import com.skytala.eCommerce.command.UpdateProductFeatureApplAttr;
import com.skytala.eCommerce.entity.ProductFeatureApplAttr;
import com.skytala.eCommerce.entity.ProductFeatureApplAttrMapper;
import com.skytala.eCommerce.event.ProductFeatureApplAttrAdded;
import com.skytala.eCommerce.event.ProductFeatureApplAttrDeleted;
import com.skytala.eCommerce.event.ProductFeatureApplAttrFound;
import com.skytala.eCommerce.event.ProductFeatureApplAttrUpdated;
import com.skytala.eCommerce.query.FindProductFeatureApplAttrsBy;

@RestController
@RequestMapping("/api/productFeatureApplAttr")
public class ProductFeatureApplAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductFeatureApplAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductFeatureApplAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductFeatureApplAttr
	 * @return a List with the ProductFeatureApplAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductFeatureApplAttr> findProductFeatureApplAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductFeatureApplAttrsBy query = new FindProductFeatureApplAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductFeatureApplAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureApplAttrFound.class,
				event -> sendProductFeatureApplAttrsFoundMessage(((ProductFeatureApplAttrFound) event).getProductFeatureApplAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductFeatureApplAttrsFoundMessage(List<ProductFeatureApplAttr> productFeatureApplAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productFeatureApplAttrs);
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
	public boolean createProductFeatureApplAttr(HttpServletRequest request) {

		ProductFeatureApplAttr productFeatureApplAttrToBeAdded = new ProductFeatureApplAttr();
		try {
			productFeatureApplAttrToBeAdded = ProductFeatureApplAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductFeatureApplAttr(productFeatureApplAttrToBeAdded);

	}

	/**
	 * creates a new ProductFeatureApplAttr entry in the ofbiz database
	 * 
	 * @param productFeatureApplAttrToBeAdded
	 *            the ProductFeatureApplAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductFeatureApplAttr(ProductFeatureApplAttr productFeatureApplAttrToBeAdded) {

		AddProductFeatureApplAttr com = new AddProductFeatureApplAttr(productFeatureApplAttrToBeAdded);
		int usedTicketId;

		synchronized (ProductFeatureApplAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureApplAttrAdded.class,
				event -> sendProductFeatureApplAttrChangedMessage(((ProductFeatureApplAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductFeatureApplAttr(HttpServletRequest request) {

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

		ProductFeatureApplAttr productFeatureApplAttrToBeUpdated = new ProductFeatureApplAttr();

		try {
			productFeatureApplAttrToBeUpdated = ProductFeatureApplAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductFeatureApplAttr(productFeatureApplAttrToBeUpdated);

	}

	/**
	 * Updates the ProductFeatureApplAttr with the specific Id
	 * 
	 * @param productFeatureApplAttrToBeUpdated the ProductFeatureApplAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductFeatureApplAttr(ProductFeatureApplAttr productFeatureApplAttrToBeUpdated) {

		UpdateProductFeatureApplAttr com = new UpdateProductFeatureApplAttr(productFeatureApplAttrToBeUpdated);

		int usedTicketId;

		synchronized (ProductFeatureApplAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureApplAttrUpdated.class,
				event -> sendProductFeatureApplAttrChangedMessage(((ProductFeatureApplAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductFeatureApplAttr from the database
	 * 
	 * @param productFeatureApplAttrId:
	 *            the id of the ProductFeatureApplAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductFeatureApplAttrById(@RequestParam(value = "productFeatureApplAttrId") String productFeatureApplAttrId) {

		DeleteProductFeatureApplAttr com = new DeleteProductFeatureApplAttr(productFeatureApplAttrId);

		int usedTicketId;

		synchronized (ProductFeatureApplAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureApplAttrDeleted.class,
				event -> sendProductFeatureApplAttrChangedMessage(((ProductFeatureApplAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductFeatureApplAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productFeatureApplAttr/\" plus one of the following: "
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

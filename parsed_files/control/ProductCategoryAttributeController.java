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
import com.skytala.eCommerce.command.AddProductCategoryAttribute;
import com.skytala.eCommerce.command.DeleteProductCategoryAttribute;
import com.skytala.eCommerce.command.UpdateProductCategoryAttribute;
import com.skytala.eCommerce.entity.ProductCategoryAttribute;
import com.skytala.eCommerce.entity.ProductCategoryAttributeMapper;
import com.skytala.eCommerce.event.ProductCategoryAttributeAdded;
import com.skytala.eCommerce.event.ProductCategoryAttributeDeleted;
import com.skytala.eCommerce.event.ProductCategoryAttributeFound;
import com.skytala.eCommerce.event.ProductCategoryAttributeUpdated;
import com.skytala.eCommerce.query.FindProductCategoryAttributesBy;

@RestController
@RequestMapping("/api/productCategoryAttribute")
public class ProductCategoryAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductCategoryAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductCategoryAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductCategoryAttribute
	 * @return a List with the ProductCategoryAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductCategoryAttribute> findProductCategoryAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductCategoryAttributesBy query = new FindProductCategoryAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductCategoryAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryAttributeFound.class,
				event -> sendProductCategoryAttributesFoundMessage(((ProductCategoryAttributeFound) event).getProductCategoryAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductCategoryAttributesFoundMessage(List<ProductCategoryAttribute> productCategoryAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productCategoryAttributes);
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
	public boolean createProductCategoryAttribute(HttpServletRequest request) {

		ProductCategoryAttribute productCategoryAttributeToBeAdded = new ProductCategoryAttribute();
		try {
			productCategoryAttributeToBeAdded = ProductCategoryAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductCategoryAttribute(productCategoryAttributeToBeAdded);

	}

	/**
	 * creates a new ProductCategoryAttribute entry in the ofbiz database
	 * 
	 * @param productCategoryAttributeToBeAdded
	 *            the ProductCategoryAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductCategoryAttribute(ProductCategoryAttribute productCategoryAttributeToBeAdded) {

		AddProductCategoryAttribute com = new AddProductCategoryAttribute(productCategoryAttributeToBeAdded);
		int usedTicketId;

		synchronized (ProductCategoryAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryAttributeAdded.class,
				event -> sendProductCategoryAttributeChangedMessage(((ProductCategoryAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductCategoryAttribute(HttpServletRequest request) {

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

		ProductCategoryAttribute productCategoryAttributeToBeUpdated = new ProductCategoryAttribute();

		try {
			productCategoryAttributeToBeUpdated = ProductCategoryAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductCategoryAttribute(productCategoryAttributeToBeUpdated);

	}

	/**
	 * Updates the ProductCategoryAttribute with the specific Id
	 * 
	 * @param productCategoryAttributeToBeUpdated the ProductCategoryAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductCategoryAttribute(ProductCategoryAttribute productCategoryAttributeToBeUpdated) {

		UpdateProductCategoryAttribute com = new UpdateProductCategoryAttribute(productCategoryAttributeToBeUpdated);

		int usedTicketId;

		synchronized (ProductCategoryAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryAttributeUpdated.class,
				event -> sendProductCategoryAttributeChangedMessage(((ProductCategoryAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductCategoryAttribute from the database
	 * 
	 * @param productCategoryAttributeId:
	 *            the id of the ProductCategoryAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductCategoryAttributeById(@RequestParam(value = "productCategoryAttributeId") String productCategoryAttributeId) {

		DeleteProductCategoryAttribute com = new DeleteProductCategoryAttribute(productCategoryAttributeId);

		int usedTicketId;

		synchronized (ProductCategoryAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCategoryAttributeDeleted.class,
				event -> sendProductCategoryAttributeChangedMessage(((ProductCategoryAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductCategoryAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productCategoryAttribute/\" plus one of the following: "
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

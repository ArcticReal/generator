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
import com.skytala.eCommerce.command.AddProductAttribute;
import com.skytala.eCommerce.command.DeleteProductAttribute;
import com.skytala.eCommerce.command.UpdateProductAttribute;
import com.skytala.eCommerce.entity.ProductAttribute;
import com.skytala.eCommerce.entity.ProductAttributeMapper;
import com.skytala.eCommerce.event.ProductAttributeAdded;
import com.skytala.eCommerce.event.ProductAttributeDeleted;
import com.skytala.eCommerce.event.ProductAttributeFound;
import com.skytala.eCommerce.event.ProductAttributeUpdated;
import com.skytala.eCommerce.query.FindProductAttributesBy;

@RestController
@RequestMapping("/api/productAttribute")
public class ProductAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductAttribute
	 * @return a List with the ProductAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductAttribute> findProductAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductAttributesBy query = new FindProductAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAttributeFound.class,
				event -> sendProductAttributesFoundMessage(((ProductAttributeFound) event).getProductAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductAttributesFoundMessage(List<ProductAttribute> productAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productAttributes);
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
	public boolean createProductAttribute(HttpServletRequest request) {

		ProductAttribute productAttributeToBeAdded = new ProductAttribute();
		try {
			productAttributeToBeAdded = ProductAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductAttribute(productAttributeToBeAdded);

	}

	/**
	 * creates a new ProductAttribute entry in the ofbiz database
	 * 
	 * @param productAttributeToBeAdded
	 *            the ProductAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductAttribute(ProductAttribute productAttributeToBeAdded) {

		AddProductAttribute com = new AddProductAttribute(productAttributeToBeAdded);
		int usedTicketId;

		synchronized (ProductAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAttributeAdded.class,
				event -> sendProductAttributeChangedMessage(((ProductAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductAttribute(HttpServletRequest request) {

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

		ProductAttribute productAttributeToBeUpdated = new ProductAttribute();

		try {
			productAttributeToBeUpdated = ProductAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductAttribute(productAttributeToBeUpdated);

	}

	/**
	 * Updates the ProductAttribute with the specific Id
	 * 
	 * @param productAttributeToBeUpdated the ProductAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductAttribute(ProductAttribute productAttributeToBeUpdated) {

		UpdateProductAttribute com = new UpdateProductAttribute(productAttributeToBeUpdated);

		int usedTicketId;

		synchronized (ProductAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAttributeUpdated.class,
				event -> sendProductAttributeChangedMessage(((ProductAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductAttribute from the database
	 * 
	 * @param productAttributeId:
	 *            the id of the ProductAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductAttributeById(@RequestParam(value = "productAttributeId") String productAttributeId) {

		DeleteProductAttribute com = new DeleteProductAttribute(productAttributeId);

		int usedTicketId;

		synchronized (ProductAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductAttributeDeleted.class,
				event -> sendProductAttributeChangedMessage(((ProductAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productAttribute/\" plus one of the following: "
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

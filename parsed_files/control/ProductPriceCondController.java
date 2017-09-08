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
import com.skytala.eCommerce.command.AddProductPriceCond;
import com.skytala.eCommerce.command.DeleteProductPriceCond;
import com.skytala.eCommerce.command.UpdateProductPriceCond;
import com.skytala.eCommerce.entity.ProductPriceCond;
import com.skytala.eCommerce.entity.ProductPriceCondMapper;
import com.skytala.eCommerce.event.ProductPriceCondAdded;
import com.skytala.eCommerce.event.ProductPriceCondDeleted;
import com.skytala.eCommerce.event.ProductPriceCondFound;
import com.skytala.eCommerce.event.ProductPriceCondUpdated;
import com.skytala.eCommerce.query.FindProductPriceCondsBy;

@RestController
@RequestMapping("/api/productPriceCond")
public class ProductPriceCondController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPriceCond>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPriceCondController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPriceCond
	 * @return a List with the ProductPriceConds
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPriceCond> findProductPriceCondsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPriceCondsBy query = new FindProductPriceCondsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPriceCondController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceCondFound.class,
				event -> sendProductPriceCondsFoundMessage(((ProductPriceCondFound) event).getProductPriceConds(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPriceCondsFoundMessage(List<ProductPriceCond> productPriceConds, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPriceConds);
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
	public boolean createProductPriceCond(HttpServletRequest request) {

		ProductPriceCond productPriceCondToBeAdded = new ProductPriceCond();
		try {
			productPriceCondToBeAdded = ProductPriceCondMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPriceCond(productPriceCondToBeAdded);

	}

	/**
	 * creates a new ProductPriceCond entry in the ofbiz database
	 * 
	 * @param productPriceCondToBeAdded
	 *            the ProductPriceCond thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPriceCond(ProductPriceCond productPriceCondToBeAdded) {

		AddProductPriceCond com = new AddProductPriceCond(productPriceCondToBeAdded);
		int usedTicketId;

		synchronized (ProductPriceCondController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceCondAdded.class,
				event -> sendProductPriceCondChangedMessage(((ProductPriceCondAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPriceCond(HttpServletRequest request) {

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

		ProductPriceCond productPriceCondToBeUpdated = new ProductPriceCond();

		try {
			productPriceCondToBeUpdated = ProductPriceCondMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPriceCond(productPriceCondToBeUpdated);

	}

	/**
	 * Updates the ProductPriceCond with the specific Id
	 * 
	 * @param productPriceCondToBeUpdated the ProductPriceCond thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPriceCond(ProductPriceCond productPriceCondToBeUpdated) {

		UpdateProductPriceCond com = new UpdateProductPriceCond(productPriceCondToBeUpdated);

		int usedTicketId;

		synchronized (ProductPriceCondController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceCondUpdated.class,
				event -> sendProductPriceCondChangedMessage(((ProductPriceCondUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPriceCond from the database
	 * 
	 * @param productPriceCondId:
	 *            the id of the ProductPriceCond thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPriceCondById(@RequestParam(value = "productPriceCondId") String productPriceCondId) {

		DeleteProductPriceCond com = new DeleteProductPriceCond(productPriceCondId);

		int usedTicketId;

		synchronized (ProductPriceCondController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceCondDeleted.class,
				event -> sendProductPriceCondChangedMessage(((ProductPriceCondDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPriceCondChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPriceCond/\" plus one of the following: "
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

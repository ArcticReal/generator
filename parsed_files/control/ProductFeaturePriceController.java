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
import com.skytala.eCommerce.command.AddProductFeaturePrice;
import com.skytala.eCommerce.command.DeleteProductFeaturePrice;
import com.skytala.eCommerce.command.UpdateProductFeaturePrice;
import com.skytala.eCommerce.entity.ProductFeaturePrice;
import com.skytala.eCommerce.entity.ProductFeaturePriceMapper;
import com.skytala.eCommerce.event.ProductFeaturePriceAdded;
import com.skytala.eCommerce.event.ProductFeaturePriceDeleted;
import com.skytala.eCommerce.event.ProductFeaturePriceFound;
import com.skytala.eCommerce.event.ProductFeaturePriceUpdated;
import com.skytala.eCommerce.query.FindProductFeaturePricesBy;

@RestController
@RequestMapping("/api/productFeaturePrice")
public class ProductFeaturePriceController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductFeaturePrice>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductFeaturePriceController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductFeaturePrice
	 * @return a List with the ProductFeaturePrices
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductFeaturePrice> findProductFeaturePricesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductFeaturePricesBy query = new FindProductFeaturePricesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductFeaturePriceController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeaturePriceFound.class,
				event -> sendProductFeaturePricesFoundMessage(((ProductFeaturePriceFound) event).getProductFeaturePrices(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductFeaturePricesFoundMessage(List<ProductFeaturePrice> productFeaturePrices, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productFeaturePrices);
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
	public boolean createProductFeaturePrice(HttpServletRequest request) {

		ProductFeaturePrice productFeaturePriceToBeAdded = new ProductFeaturePrice();
		try {
			productFeaturePriceToBeAdded = ProductFeaturePriceMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductFeaturePrice(productFeaturePriceToBeAdded);

	}

	/**
	 * creates a new ProductFeaturePrice entry in the ofbiz database
	 * 
	 * @param productFeaturePriceToBeAdded
	 *            the ProductFeaturePrice thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductFeaturePrice(ProductFeaturePrice productFeaturePriceToBeAdded) {

		AddProductFeaturePrice com = new AddProductFeaturePrice(productFeaturePriceToBeAdded);
		int usedTicketId;

		synchronized (ProductFeaturePriceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeaturePriceAdded.class,
				event -> sendProductFeaturePriceChangedMessage(((ProductFeaturePriceAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductFeaturePrice(HttpServletRequest request) {

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

		ProductFeaturePrice productFeaturePriceToBeUpdated = new ProductFeaturePrice();

		try {
			productFeaturePriceToBeUpdated = ProductFeaturePriceMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductFeaturePrice(productFeaturePriceToBeUpdated);

	}

	/**
	 * Updates the ProductFeaturePrice with the specific Id
	 * 
	 * @param productFeaturePriceToBeUpdated the ProductFeaturePrice thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductFeaturePrice(ProductFeaturePrice productFeaturePriceToBeUpdated) {

		UpdateProductFeaturePrice com = new UpdateProductFeaturePrice(productFeaturePriceToBeUpdated);

		int usedTicketId;

		synchronized (ProductFeaturePriceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeaturePriceUpdated.class,
				event -> sendProductFeaturePriceChangedMessage(((ProductFeaturePriceUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductFeaturePrice from the database
	 * 
	 * @param productFeaturePriceId:
	 *            the id of the ProductFeaturePrice thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductFeaturePriceById(@RequestParam(value = "productFeaturePriceId") String productFeaturePriceId) {

		DeleteProductFeaturePrice com = new DeleteProductFeaturePrice(productFeaturePriceId);

		int usedTicketId;

		synchronized (ProductFeaturePriceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeaturePriceDeleted.class,
				event -> sendProductFeaturePriceChangedMessage(((ProductFeaturePriceDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductFeaturePriceChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productFeaturePrice/\" plus one of the following: "
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

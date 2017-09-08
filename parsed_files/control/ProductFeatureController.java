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
import com.skytala.eCommerce.command.AddProductFeature;
import com.skytala.eCommerce.command.DeleteProductFeature;
import com.skytala.eCommerce.command.UpdateProductFeature;
import com.skytala.eCommerce.entity.ProductFeature;
import com.skytala.eCommerce.entity.ProductFeatureMapper;
import com.skytala.eCommerce.event.ProductFeatureAdded;
import com.skytala.eCommerce.event.ProductFeatureDeleted;
import com.skytala.eCommerce.event.ProductFeatureFound;
import com.skytala.eCommerce.event.ProductFeatureUpdated;
import com.skytala.eCommerce.query.FindProductFeaturesBy;

@RestController
@RequestMapping("/api/productFeature")
public class ProductFeatureController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductFeature>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductFeatureController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductFeature
	 * @return a List with the ProductFeatures
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductFeature> findProductFeaturesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductFeaturesBy query = new FindProductFeaturesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductFeatureController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureFound.class,
				event -> sendProductFeaturesFoundMessage(((ProductFeatureFound) event).getProductFeatures(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductFeaturesFoundMessage(List<ProductFeature> productFeatures, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productFeatures);
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
	public boolean createProductFeature(HttpServletRequest request) {

		ProductFeature productFeatureToBeAdded = new ProductFeature();
		try {
			productFeatureToBeAdded = ProductFeatureMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductFeature(productFeatureToBeAdded);

	}

	/**
	 * creates a new ProductFeature entry in the ofbiz database
	 * 
	 * @param productFeatureToBeAdded
	 *            the ProductFeature thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductFeature(ProductFeature productFeatureToBeAdded) {

		AddProductFeature com = new AddProductFeature(productFeatureToBeAdded);
		int usedTicketId;

		synchronized (ProductFeatureController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureAdded.class,
				event -> sendProductFeatureChangedMessage(((ProductFeatureAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductFeature(HttpServletRequest request) {

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

		ProductFeature productFeatureToBeUpdated = new ProductFeature();

		try {
			productFeatureToBeUpdated = ProductFeatureMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductFeature(productFeatureToBeUpdated);

	}

	/**
	 * Updates the ProductFeature with the specific Id
	 * 
	 * @param productFeatureToBeUpdated the ProductFeature thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductFeature(ProductFeature productFeatureToBeUpdated) {

		UpdateProductFeature com = new UpdateProductFeature(productFeatureToBeUpdated);

		int usedTicketId;

		synchronized (ProductFeatureController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureUpdated.class,
				event -> sendProductFeatureChangedMessage(((ProductFeatureUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductFeature from the database
	 * 
	 * @param productFeatureId:
	 *            the id of the ProductFeature thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductFeatureById(@RequestParam(value = "productFeatureId") String productFeatureId) {

		DeleteProductFeature com = new DeleteProductFeature(productFeatureId);

		int usedTicketId;

		synchronized (ProductFeatureController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureDeleted.class,
				event -> sendProductFeatureChangedMessage(((ProductFeatureDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductFeatureChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productFeature/\" plus one of the following: "
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

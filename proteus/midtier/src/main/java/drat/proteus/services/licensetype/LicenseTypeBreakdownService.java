package drat.proteus.services.licensetype;

import drat.proteus.services.general.Item;
import drat.proteus.services.product.BaseProductService;
import drat.proteus.services.product.ProductItem;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LicenseTypeBreakdownService extends BaseProductService {
    private static final String CHANNEL = "RatLog";
    private static final String TYPE_ID = "urn:drat:RatLog";

    private static Client client = ClientBuilder.newBuilder().newClient();
    public LicenseTypeBreakdownService() {

    }

    public List<Item> getLicenseTypes() {
        List<Item> ratLogProducts = super.getRecentProductsByChannelAndTypeId(CHANNEL, TYPE_ID);
        RatAggregator aggregator = new RatAggregator();
        for(Item item: ratLogProducts) {
            RatLog log = getLicenseTypesFromRatLog(((ProductItem) item).getLink());
            aggregator.add(log);
        }
        Map<String, Integer> aggregate = aggregator.getAggregatedLicenseTotal();
        List<Item> breakdownItems = new ArrayList<>();
        int totalLicensesInRepo = 0;
        for(Integer count : aggregate.values()) {
            totalLicensesInRepo += count;
        }
        for(String license: aggregate.keySet()) {
            Integer numberOfLicenses = aggregate.get(license);
            LicenseTypeBreakdownItem breakdownItem = new LicenseTypeBreakdownItem(license, numberOfLicenses, totalLicensesInRepo);
            breakdownItems.add(breakdownItem);
        }
        return breakdownItems;
    }

    private RatLog getLicenseTypesFromRatLog(String ratLogLink) {
        WebTarget target = client.target(ratLogLink);
        Invocation.Builder builder = target.request();
        Response res = builder.get();
        return new RatLog(res.readEntity(String.class));
    }
}

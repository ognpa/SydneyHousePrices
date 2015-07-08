package australia.sydney.houseprices;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/***
 * Utility to hit realestateview and get the data for each suburb and store it
 * 
 * @author priyaananthram
 *
 */
public class DataScraper {

	private Map<String, String> suburbMap = new HashMap<String, String>();
	private String url = "http://www.realestateview.com.au/portal/propertydata?rm=suburblinks&state=nsw&webservice=medianprice&startletter=";
	private String medianUrl = "http://www.realestateview.com.au/portal/propertydata?rm=creategraphurl&median_state=nsw&median_type=house&median_period=5&median_suburb=";
	private HashMap<String, MedianHousePrices[]> medianHousePricesMap = new HashMap<String, MedianHousePrices[]>();

	private String medianUnitUrl = "http://www.realestateview.com.au/portal/propertydata?rm=creategraphurl&median_state=nsw&median_type=unit&median_period=5&median_suburb=";
	private final String CHX = "chxl=0:";
	private final String CHXS = "chxs";

	private final String COLON = ":";

	private final String AMPERSAND = "&";
	private final String CHD = "chd=t:";
	private final String CHDL = "chdl";

	/**
	 * Get list of suburbs given alphabet
	 * 
	 * @param alphabet
	 * @throws IOException
	 */
	public void getAllSuburbs(char alphabet) throws IOException {
		Document doc = Jsoup.connect(url + alphabet).timeout(0).get();

		Elements suburbList = doc.select("p.pd-suburb-list");
		for (Element suburb : suburbList) {
			Element ahref = suburb.select("a").first();
			String text = ahref.text();
			// If the text has a space then replace with +
			text = text.replace(" ", "+").toLowerCase();
			String newLink = medianUrl + text;

			if (!text.trim().equals(""))
				suburbMap.put(text, newLink);

		}

	}

	/**
	 * Helper method to print data
	 */
	public void printData() {
		for (String sub : suburbMap.keySet()) {
			String url = suburbMap.get(sub);
			System.out.println(sub + "  " + url);

		}

	}

	/**
	 * Helper method to print median prices
	 */
	public void printMedianPrices() {
		for (String sub : medianHousePricesMap.keySet()) {
			MedianHousePrices[] mhp = medianHousePricesMap.get(sub);
			System.out.println("Suburb " + sub);
			for (MedianHousePrices m : mhp) {
				System.out.println(m.getMonth() + "  " + m.getYear() + "   "
						+ m.getMedianPrice());
			}
		}
	}

	/**
	 * Get median house prices
	 * 
	 * @throws IOException
	 */
	private void getMedianData() throws IOException {
		for (String sub : suburbMap.keySet()) {
			MedianHousePrices[] mhp = getMedian(suburbMap.get(sub));
			if (mhp == null) {// Try flat data
				String url = medianUnitUrl + sub;
				mhp = getMedian(url);
			}
			medianHousePricesMap.put(sub, mhp);
		}
	}

	/**
	 * Get median houseprice given url
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */

	public MedianHousePrices[] getMedian(String url) throws IOException {
		Document doc = Jsoup.connect(url).timeout(0).get();
		// median_graph
		Elements medianGraph = doc.select("img#median_graph");

		MedianHousePrices[] output = null;
		for (Element ele : medianGraph) {
			String src = ele.attr("src");
			System.out.println(src);
			output = generateMedianPrices(src);

		}

		return output;

	}
/**
 * Method to parse coordinates in img field we are looking for CHD it usually has x and y coordinates we are looking for y coordinates
 * @param src
 * @return
 */
	public MedianHousePrices[] generateMedianPrices(String src) {
		// Look for chd
		int firstLabel = src.indexOf(CHX);
		int endLabel = src.indexOf(CHXS);

		String yLabel = src.substring(firstLabel + (CHX).length(), endLabel);
		yLabel = yLabel.replaceAll(AMPERSAND, "").trim();
		String[] yLabelArray = yLabel.split(COLON);

		String yAxisMonthLabel = yLabelArray[0];

		String mthLabel = yAxisMonthLabel.substring(0,
				yAxisMonthLabel.length() - 1);
		String yAxisYearLabel = yLabelArray[2];
		String yrLabel = yAxisYearLabel.substring(1, yAxisYearLabel.length());
		String[] monthLabels = mthLabel.replace("|", "-").split("-");
		String[] yearLabels = yrLabel.replace("|", "-").split("-");

		int first = src.indexOf(CHD);
		int end = src.indexOf(CHDL);

		String sub = src.substring(first + CHD.length(), end).replace("|",
				COLON);
		sub = sub.replaceAll(AMPERSAND, "").trim();
		String[] yCoords = sub.split(COLON);
		String yCoordinate = yCoords[1];
		String[] medianPrices = yCoordinate.split(",");
		MedianHousePrices[] output = new MedianHousePrices[medianPrices.length];
		for (int i = 0; i < medianPrices.length; i++) {
			String year = (yearLabels[i]);
			String mth = monthLabels[i];
			MedianHousePrices mhp = new MedianHousePrices(mth, year,
					(medianPrices[i]));
			output[i] = mhp;
		}
		return output;

	}

	class MedianHousePrices {
		String month;
		String year;
		String medianPrice;

		@Override
		public String toString() {
			return "MedianHousePrices [month=" + month + ", year=" + year
					+ ", medianPrice=" + medianPrice + "]";
		}

		public String getMonth() {
			return month;
		}

		public void setMonth(String month) {
			this.month = month;
		}

		public String getYear() {
			return year;
		}

		public void setYear(String year) {
			this.year = year;
		}

		public String getMedianPrice() {
			return medianPrice;
		}

		public void setMedianPrice(String medianPrice) {
			this.medianPrice = medianPrice;
		}

		public MedianHousePrices(String month, String year, String medianPrice) {
			super();
			this.month = month;
			this.year = year;
			this.medianPrice = medianPrice;
		}

	}

	public void getData() throws IOException {

		for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
			getAllSuburbs(alphabet);
		}
	}

	public static void main(String args[]) {
		DataScraper ds = new DataScraper();
		

		try {
			ds.getData();
			ds.getMedianData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ds.printMedianPrices();

	}

}

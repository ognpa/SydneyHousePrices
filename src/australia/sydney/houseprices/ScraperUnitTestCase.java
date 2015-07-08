package australia.sydney.houseprices;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import australia.sydney.houseprices.DataScraper.MedianHousePrices;

public class ScraperUnitTestCase {

	@Test
	public void testIfHousePricesScrappedProperly() {
		String src = "http://chart.apis.google.com/chart?cht=lxy&amp;chs=575x300&amp;chxt=x,y,x&amp;chxl=0:|Sep|Dec|Mar|Jun|1:|$374K|$405K|$436K|$467K|2:|09|09|10|10&amp;"
				+ "chxs=0,,10|1,,10&amp;chf=bg,lg,90,E1E4E5,0,F3F2F3,0.5&amp;chma=4,4,4,4&amp;chds=374000,964368&amp;chg=5.26315789473684,5.26315789473684&amp;"
				+ "chd=t:374000,405072,436144,467216|-1000000,-1000000,-1000000,675000&amp;chdl=WESTMEAD&amp;chdlp=b|l&amp;chco=0000FF,09A35D,D2691E,8B008B,008000,DC143C,483D8B,A52A2A,FF1493,800080,000000,FF0000,FF1493,2F4F4F,E9967A,B8860B,BDB76B,696969,4B0082";

		DataScraper ds = new DataScraper();
		MedianHousePrices[] gotArray = ds.generateMedianPrices(src);
		MedianHousePrices[] mhp = new MedianHousePrices[4];
		mhp[0] = ds.new MedianHousePrices("Sep", "09", "-1000000");
		mhp[1] = ds.new MedianHousePrices("Dec", "09", "-1000000");
		mhp[2] = ds.new MedianHousePrices("Mar", "10", "-1000000");
		mhp[3] = ds.new MedianHousePrices("Jun", "10", "675000");

		for (int i = 0; i < gotArray.length; i++) {
			MedianHousePrices got = gotArray[i];
			MedianHousePrices required = mhp[i];
			assertEquals(got.getMedianPrice(), required.getMedianPrice());
			assertEquals(got.getMonth(), required.getMonth());
			assertEquals(got.getYear(), required.getYear());

		}

	}

}

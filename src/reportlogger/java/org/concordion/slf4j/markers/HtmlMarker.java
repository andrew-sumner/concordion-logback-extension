package org.concordion.slf4j.markers;

public class HtmlMarker extends BaseDataMarker<HtmlMarker> {
	private static final long serialVersionUID = 5412731321120168078L;
	
	public HtmlMarker(String html) {
		super(html);
	}

	@Override
	public String getFormattedData() {
		return data;
	}

	@Override
	public void prepareData() {

	}
}
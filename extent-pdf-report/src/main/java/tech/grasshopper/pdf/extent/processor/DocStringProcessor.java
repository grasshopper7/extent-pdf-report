package tech.grasshopper.pdf.extent.processor;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.model.Log;

import lombok.Builder;
import lombok.Builder.Default;

@Builder
public class DocStringProcessor {

	@Default
	private List<Log> logs = new ArrayList<>();

	public String process() {

		for (Log log : logs) {

			if (log.getStatus() != Status.PASS)
				continue;
			
			String html = log.getDetails();
			Document doc = Jsoup.parseBodyFragment(html);

			Element element = doc.selectFirst("body textarea[class*=\"code-block\"]");

			if (element != null)
				return element.text();
		}
		return "";
	}
}

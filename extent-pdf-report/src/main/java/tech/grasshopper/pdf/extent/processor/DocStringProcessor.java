package tech.grasshopper.pdf.extent.processor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.model.Log;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class DocStringProcessor extends Processor {

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

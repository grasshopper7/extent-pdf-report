package tech.grasshopper.pdf.extent.processor;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.model.Log;

import lombok.experimental.SuperBuilder;
import tech.grasshopper.pdf.pojo.cucumber.Row;

@SuperBuilder
public class DataTableProcessor extends Processor {

	public List<Row> process() {

		List<Row> data = new ArrayList<>();

		for (Log log : logs) {

			if (log.getStatus() != Status.PASS)
				continue;

			String html = log.getDetails();
			Document doc = Jsoup.parseBodyFragment(html);

			Element element = doc.selectFirst("body > table[class*=\"markup-table table\"]");

			if (element != null) {
				Elements rowElements = element.select("tr");

				for (Element rowElement : rowElements) {

					List<String> cells = new ArrayList<>();
					data.add(Row.builder().cells(cells).build());

					for (Element cell : rowElement.select("td")) {
						cells.add(cell.text());
					}
				}
			}
		}
		return data;
	}
}

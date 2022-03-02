package tech.grasshopper.pdf.extent.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.aventstack.extentreports.model.Log;

import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class StackTraceProcessor extends Processor {

	@Default
	private List<Log> logs = new ArrayList<>();

	public String process() {

		List<Log> failAndSkipLogs = logs.stream().filter(l -> l.getStatus() == com.aventstack.extentreports.Status.FAIL
				|| l.getStatus() == com.aventstack.extentreports.Status.SKIP).collect(Collectors.toList());

		String stack = "";

		for (Log log : failAndSkipLogs) {

			// For adapter which stores failure as throwable
			if (log.getException() != null)
				stack = log.getException().getStackTrace();
			// For json plugin which stores failure as markup string
			else
				stack = stripMarkup(log.getDetails());
		}
		return stack;
	}

	private String stripMarkup(String markup) {
		if (markup == null || markup.isEmpty())
			return "";
		int start = markup.indexOf(">", markup.indexOf("<textarea"));
		int end = markup.indexOf("</textarea");
		if (start == -1 || end == -1)
			return markup;
		return markup.substring(start + 1, end);
	}
}

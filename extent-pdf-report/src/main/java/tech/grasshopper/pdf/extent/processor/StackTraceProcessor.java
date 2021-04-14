package tech.grasshopper.pdf.extent.processor;

import java.util.ArrayList;
import java.util.List;

import com.aventstack.extentreports.model.Log;

import lombok.Builder;
import lombok.Builder.Default;

@Builder
public class StackTraceProcessor {

	@Default
	private List<Log> logs = new ArrayList<>();

	public String process() {

		String stack = "";
		for (Log log : logs) {
			if (log.getStatus() == com.aventstack.extentreports.Status.FAIL
					|| log.getStatus() == com.aventstack.extentreports.Status.SKIP) {
				// For adapter which stores failure as throwable
				if (log.getException() != null)
					stack = log.getException().getStackTrace();
				// For json plugin which stores failure as markup string
				else
					stack = stripMarkup(log.getDetails());
			}
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

package tech.grasshopper.pdf.extent.processor;

import java.util.ArrayList;
import java.util.List;

import com.aventstack.extentreports.model.Log;

import lombok.Builder;
import lombok.Builder.Default;

@Builder
public class LogMessageProcessor {
	
	@Default
	private List<Log> logs = new ArrayList<>();

	public List<String> process() {
		
		List<String> output = new ArrayList<>();
		for (Log log : logs) {
			if (log.getStatus() == com.aventstack.extentreports.Status.INFO && !log.getDetails().isEmpty())
				output.add(log.getDetails());
		}
		return output;
	}
}

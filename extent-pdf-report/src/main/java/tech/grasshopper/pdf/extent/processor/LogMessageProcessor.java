package tech.grasshopper.pdf.extent.processor;

import java.util.ArrayList;
import java.util.List;

import com.aventstack.extentreports.model.Log;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class LogMessageProcessor extends Processor {

	public List<String> process() {

		List<String> output = new ArrayList<>();
		for (Log log : logs) {
			if (log.getStatus() == com.aventstack.extentreports.Status.INFO && !log.getDetails().isEmpty())
				output.add(log.getDetails());
		}
		return output;
	}
}

package tech.grasshopper.pdf.extent.processor;

import java.util.ArrayList;
import java.util.List;

import com.aventstack.extentreports.model.Log;

import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class Processor {

	@Default
	protected List<Log> logs = new ArrayList<>();

	public abstract Object process();
}

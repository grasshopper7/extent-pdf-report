package tech.grasshopper.pdf.extent.processor.data;

import com.aventstack.extentreports.model.Test;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class AdditionalDataProcessor {

	public abstract Object process(Test test);

	public abstract String getDataProcessorKey();
}

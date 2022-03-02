package tech.grasshopper.pdf.extent.processor.data;

import com.aventstack.extentreports.model.Test;

import lombok.experimental.SuperBuilder;
import tech.grasshopper.pdf.pojo.cucumber.AdditionalDataKey;

@SuperBuilder
public class RestAssuredAdditionalDataProcessor extends AdditionalDataProcessor {

	@Override
	public Object process(Test test) {
		return test.getInfoMap().get(AdditionalDataKey.REST_ASSURED_DATA_KEY);
	}

	@Override
	public String getDataProcessorKey() {
		return AdditionalDataKey.REST_ASSURED_DATA_KEY;
	}
}

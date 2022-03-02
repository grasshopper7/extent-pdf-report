package tech.grasshopper.pdf.extent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aventstack.extentreports.gherkin.model.Asterisk;
import com.aventstack.extentreports.gherkin.model.ScenarioOutline;
import com.aventstack.extentreports.model.Report;
import com.aventstack.extentreports.model.Test;

import lombok.Builder;
import lombok.Builder.Default;
import tech.grasshopper.pdf.data.ReportData;
import tech.grasshopper.pdf.extent.processor.DataTableProcessor;
import tech.grasshopper.pdf.extent.processor.DocStringProcessor;
import tech.grasshopper.pdf.extent.processor.LogMessageProcessor;
import tech.grasshopper.pdf.extent.processor.MediaProcessor;
import tech.grasshopper.pdf.extent.processor.StackTraceProcessor;
import tech.grasshopper.pdf.extent.processor.data.AdditionalDataProcessor;
import tech.grasshopper.pdf.pojo.cucumber.Feature;
import tech.grasshopper.pdf.pojo.cucumber.Hook;
import tech.grasshopper.pdf.pojo.cucumber.Hook.HookType;
import tech.grasshopper.pdf.pojo.cucumber.Row;
import tech.grasshopper.pdf.pojo.cucumber.Scenario;
import tech.grasshopper.pdf.pojo.cucumber.Status;
import tech.grasshopper.pdf.pojo.cucumber.Step;
import tech.grasshopper.pdf.util.DateUtil;

@Builder
public class ExtentPDFReportDataGenerator {

	@Default
	private String mediaFolder = "";

	@Default
	private List<AdditionalDataProcessor> featureAddDataProcessors = new ArrayList();

	@Default
	private List<AdditionalDataProcessor> scenarioAddDataProcessors = new ArrayList();

	public ReportData generateReportData(Report report) {
		List<Test> extentTests = report.getTestList();
		List<Feature> features = new ArrayList<>();

		for (Test featureTest : extentTests) {

			List<String> featureTags = new ArrayList<>();
			featureTest.getCategorySet().stream().forEach(c -> featureTags.add(c.getName()));

			List<Scenario> scenarios = new ArrayList<>();
			Feature feature = Feature.builder().name(featureTest.getName())
					.status(convertStatus(featureTest.getStatus())).tags(featureTags).scenarios(scenarios)
					.startTime(DateUtil.convertToLocalDateTimeFromDate(featureTest.getStartTime()))
					.endTime(DateUtil.convertToLocalDateTimeFromDate(featureTest.getEndTime()))
					.additionalData(getFeatureAdditionalData(featureTest)).build();
			features.add(feature);

			for (Test scenarioTest : featureTest.getChildren()) {

				if (scenarioTest.getBddType() == ScenarioOutline.class) {
					for (Test soScenarioTest : scenarioTest.getChildren())
						createScenarioHookSteps(soScenarioTest, scenarios);
				} else
					createScenarioHookSteps(scenarioTest, scenarios);
			}
		}
		return ReportData.builder().features(features).build();
	}

	private void createScenarioHookSteps(Test scenarioTest, List<Scenario> scenarios) {

		List<Step> steps = new ArrayList<>();
		List<Hook> beforeHook = new ArrayList<>();
		List<Hook> afterHook = new ArrayList<>();

		List<String> scenarioTags = new ArrayList<>();
		scenarioTest.getCategorySet().stream().forEach(c -> scenarioTags.add(c.getName()));

		Scenario scenario = Scenario.builder().name(scenarioTest.getName())
				.status(convertStatus(scenarioTest.getStatus())).tags(scenarioTags).steps(steps).before(beforeHook)
				.after(afterHook).startTime(DateUtil.convertToLocalDateTimeFromDate(scenarioTest.getStartTime()))
				.endTime(DateUtil.convertToLocalDateTimeFromDate(scenarioTest.getEndTime()))
				.additionalData(getScenarioAdditionalData(scenarioTest)).build();
		scenarios.add(scenario);

		Step step = null;
		LoopObject loopObject = LoopObject.INITIAL;
		for (Test stepTest : scenarioTest.getChildren()) {

			if (stepTest.getBddType() == Asterisk.class && isValidHook(stepTest)) {
				HookType type = HookType.valueOf(stepTest.getDescription().toUpperCase());
				switch (type) {
				case BEFORE:
					addHookData(beforeHook, stepTest);
					break;
				case AFTER:
					addHookData(afterHook, stepTest);
					break;
				case BEFORE_STEP:
					if (loopObject == LoopObject.INITIAL || loopObject == LoopObject.STEP
							|| loopObject == LoopObject.AFTER_STEP) {
						step = Step.builder().build();
					}

					step.addBeforeStepHook(createHook(stepTest));
					loopObject = LoopObject.BEFORE_STEP;
					break;
				case AFTER_STEP:
					step.addAfterStepHook(createHook(stepTest));
					loopObject = LoopObject.AFTER_STEP;
					break;
				}
			} else {
				if (loopObject == LoopObject.INITIAL || loopObject == LoopObject.STEP
						|| loopObject == LoopObject.AFTER_STEP) {
					step = Step.builder().build();
				}

				addStepData(step, stepTest);
				steps.add(step);
				loopObject = LoopObject.STEP;
			}
		}
	}

	private void addStepData(Step step, Test stepTest) {
		step.setName(stepTest.getName());
		step.setStatus(convertStatus(stepTest.getStatus()));
		step.setKeyword(stepTest.getBddType().getSimpleName());
		step.setErrorMessage(getStackTrace(stepTest));
		step.setOutput(getLogMessages(stepTest));
		step.setMedia(getMediaData(stepTest));
		step.setStartTime(DateUtil.convertToLocalDateTimeFromDate(stepTest.getStartTime()));
		step.setEndTime(DateUtil.convertToLocalDateTimeFromDate(stepTest.getEndTime()));
		step.setDocString(getDocString(stepTest));
		step.setRows(getDataTable(stepTest));
	}

	private void addHookData(List<Hook> hooks, Test hookTest) {
		hooks.add(createHook(hookTest));
	}

	private Hook createHook(Test hookTest) {
		return Hook.builder().location(hookTest.getName()).hookType(HookType.valueOf(hookTest.getDescription()))
				.status(convertStatus(hookTest.getStatus())).errorMessage(getStackTrace(hookTest))
				.output(getLogMessages(hookTest)).media(getMediaData(hookTest))
				.startTime(DateUtil.convertToLocalDateTimeFromDate(hookTest.getStartTime()))
				.endTime(DateUtil.convertToLocalDateTimeFromDate(hookTest.getEndTime())).build();
	}

	private Status convertStatus(com.aventstack.extentreports.Status extentStatus) {
		Status status = Status.SKIPPED;
		if (extentStatus == com.aventstack.extentreports.Status.PASS)
			status = Status.PASSED;
		else if (extentStatus == com.aventstack.extentreports.Status.FAIL)
			status = Status.FAILED;
		return status;
	}

	private boolean isValidHook(Test test) {

		return Arrays.stream(HookType.values()).anyMatch((h) -> h.name().equals(test.getDescription().toUpperCase()));
	}

	private List<Row> getDataTable(Test test) {

		return DataTableProcessor.builder().logs(test.getLogs()).build().process();
	}

	private String getDocString(Test test) {

		return DocStringProcessor.builder().logs(test.getLogs()).build().process();
	}

	private String getStackTrace(Test test) {

		return StackTraceProcessor.builder().logs(test.getLogs()).build().process();
	}

	private List<String> getLogMessages(Test test) {

		return LogMessageProcessor.builder().logs(test.getLogs()).build().process();
	}

	private List<String> getMediaData(Test test) {

		return MediaProcessor.builder().logs(test.getLogs()).mediaFolder(mediaFolder).build().process();
	}

	private Map<String, Object> getFeatureAdditionalData(Test test) {

		return getAdditionalData(test, featureAddDataProcessors);
	}

	private Map<String, Object> getScenarioAdditionalData(Test test) {

		return getAdditionalData(test, scenarioAddDataProcessors);
	}

	private Map<String, Object> getAdditionalData(Test test, List<AdditionalDataProcessor> processors) {
		Map<String, Object> data = new HashMap<>();

		for (AdditionalDataProcessor processor : processors)
			data.put(processor.getDataProcessorKey(), processor.process(test));
		return data;
	}

	private static enum LoopObject {
		INITIAL, BEFORE_STEP, STEP, AFTER_STEP;
	}
}

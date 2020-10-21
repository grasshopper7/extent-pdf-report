package tech.grasshopper.pdf.extent;

import java.util.ArrayList;
import java.util.List;

import com.aventstack.extentreports.gherkin.model.Asterisk;
import com.aventstack.extentreports.gherkin.model.ScenarioOutline;
import com.aventstack.extentreports.model.Log;
import com.aventstack.extentreports.model.Report;
import com.aventstack.extentreports.model.Test;

import tech.grasshopper.pdf.data.ReportData;
import tech.grasshopper.pdf.pojo.cucumber.Feature;
import tech.grasshopper.pdf.pojo.cucumber.Hook;
import tech.grasshopper.pdf.pojo.cucumber.Hook.HookType;
import tech.grasshopper.pdf.pojo.cucumber.Scenario;
import tech.grasshopper.pdf.pojo.cucumber.Status;
import tech.grasshopper.pdf.pojo.cucumber.Step;
import tech.grasshopper.pdf.util.DateUtil;

public class ExtentPDFReportDataGenerator {

	public ReportData generateReportData(Report report) {
		List<Test> extentTests = report.getTestList();
		List<Feature> features = new ArrayList<>();

		for (Test featureTest : extentTests) {

			List<String> featureTags = new ArrayList<>();
			featureTest.getCategorySet().stream().forEach(c -> featureTags.add(c.getName()));

			List<Scenario> scenarios = new ArrayList<>();
			Feature feature = Feature.builder().name(featureTest.getName())
					.status(convertStatus(featureTest.getStatus())).tags(featureTags).scenarios(scenarios)
					.startTime(DateUtil.convertToLocalDateTime(featureTest.getStartTime()))
					.endTime(DateUtil.convertToLocalDateTime(featureTest.getEndTime())).build();
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
				.after(afterHook).startTime(DateUtil.convertToLocalDateTime(scenarioTest.getStartTime()))
				.endTime(DateUtil.convertToLocalDateTime(scenarioTest.getEndTime())).build();
		scenarios.add(scenario);

		List<Hook> beforeStepHook = new ArrayList<>();
		List<Hook> afterStepHook = new ArrayList<>();
		boolean newStep = true;

		for (Test stepTest : scenarioTest.getChildren()) {

			if (stepTest.getBddType() == Asterisk.class) {
				HookType type = HookType.valueOf(stepTest.getDescription().toUpperCase());
				switch (type) {
				case BEFORE:
					addHookData(beforeHook, stepTest);
					break;
				case AFTER:
					addHookData(afterHook, stepTest);
					break;
				case BEFORE_STEP:
					if (newStep) {
						newStep = false;
						beforeStepHook = new ArrayList<>();
						afterStepHook = new ArrayList<>();
					}
					addHookData(beforeStepHook, stepTest);
					break;
				case AFTER_STEP:
					newStep = true;
					addHookData(afterStepHook, stepTest);
					break;
				}
			} else {
				Step step = Step.builder().name(stepTest.getName()).status(convertStatus(stepTest.getStatus()))
						.keyword(stepTest.getBddType().getSimpleName()).errorMessage(getStackTrace(stepTest))
						.output(getLogMessages(stepTest)).media(getMediaData(stepTest)).before(beforeStepHook).after(afterStepHook)
						.startTime(DateUtil.convertToLocalDateTime(stepTest.getStartTime()))
						.endTime(DateUtil.convertToLocalDateTime(stepTest.getEndTime())).build();
				steps.add(step);
			}
		}
	}

	private void addHookData(List<Hook> hooks, Test hookTest) {
		hooks.add(Hook.builder().location(hookTest.getName()).hookType(HookType.valueOf(hookTest.getDescription()))
				.status(convertStatus(hookTest.getStatus())).errorMessage(getStackTrace(hookTest))
				.output(getLogMessages(hookTest)).media(getMediaData(hookTest)).startTime(DateUtil.convertToLocalDateTime(hookTest.getStartTime()))
				.endTime(DateUtil.convertToLocalDateTime(hookTest.getEndTime())).build());
	}

	private Status convertStatus(com.aventstack.extentreports.Status extentStatus) {
		Status status = Status.SKIPPED;
		if (extentStatus == com.aventstack.extentreports.Status.PASS)
			status = Status.PASSED;
		else if (extentStatus == com.aventstack.extentreports.Status.FAIL)
			status = Status.FAILED;
		return status;
	}

	private String getStackTrace(Test test) {
		String stack = "";
		for (Log log : test.getLogs()) {
			if (log.getException() != null)
				stack = log.getException().getStackTrace();
		}
		return stack;
	}

	private List<String> getLogMessages(Test test) {
		List<String> output = new ArrayList<>();
		for (Log log : test.getLogs()) {
			if (log.getStatus() == com.aventstack.extentreports.Status.INFO && !log.getDetails().isEmpty())
				output.add(log.getDetails());
		}
		return output;
	}
	
	private List<String> getMediaData(Test test) {
		List<String> media = new ArrayList<>();
		for (Log log : test.getLogs()) {
			if (log.getStatus() == com.aventstack.extentreports.Status.INFO && log.getMedia() != null)
				media.add(log.getMedia().getPath());
		}
		return media;
	}
}

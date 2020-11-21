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
					.startTime(DateUtil.convertToLocalDateTimeFromDate(featureTest.getStartTime()))
					.endTime(DateUtil.convertToLocalDateTimeFromDate(featureTest.getEndTime())).build();
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
				.endTime(DateUtil.convertToLocalDateTimeFromDate(scenarioTest.getEndTime())).build();
		scenarios.add(scenario);

		Step step = null;
		LoopObject loopObject = LoopObject.INITIAL;
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

	private String getStackTrace(Test test) {
		String stack = "";
		for (Log log : test.getLogs()) {
			if (log.getStatus() == com.aventstack.extentreports.Status.FAIL) {
				//For adapter which stores failure as throwable
				if (log.getException() != null)
					stack = log.getException().getStackTrace();
				//For json plugin which stores failure as markup string
				else
					stack = stripMarkup(log.getDetails());
			}
		}
		return stack;
	}
	
	private String stripMarkup(String markup) {
		int start = markup.indexOf(">",markup.indexOf("<textarea"));
		int end = markup.indexOf("</textarea");
		if(start == -1 || end == -1)
			return markup;
		return markup.substring(start + 1, end);
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

	private static enum LoopObject {
		INITIAL, BEFORE_STEP, STEP, AFTER_STEP;
	}
}

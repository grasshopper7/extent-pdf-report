package tech.grasshopper.pdf.extent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.aventstack.extentreports.model.Report;
import com.aventstack.extentreports.observer.ReportObserver;
import com.aventstack.extentreports.observer.entity.ReportEntity;
import com.aventstack.extentreports.reporter.AbstractFileReporter;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import tech.grasshopper.pdf.RestAssuredPdfCucumberReport;
import tech.grasshopper.pdf.data.ReportData;
import tech.grasshopper.pdf.extent.processor.data.AdditionalDataProcessor;
import tech.grasshopper.pdf.extent.processor.data.RestAssuredAdditionalDataProcessor;

public class RestAssuredExtentPDFCucumberReporter extends AbstractFileReporter implements ReportObserver<ReportEntity> {

	private static final Logger logger = Logger.getLogger(RestAssuredExtentPDFCucumberReporter.class.getName());
	private static final String REPORTER_NAME = "rapdf";
	private static final String FILE_NAME = "Index.pdf";

	private Disposable disposable;
	private Report report;

	public RestAssuredExtentPDFCucumberReporter(String path) {
		super(new File(path));
	}

	public RestAssuredExtentPDFCucumberReporter(File f) {
		super(f);
	}

	public Observer<ReportEntity> getReportObserver() {
		return new Observer<ReportEntity>() {

			public void onSubscribe(Disposable d) {
				start(d);
			}

			public void onNext(ReportEntity value) {
				flush(value);
			}

			public void onError(Throwable e) {
			}

			public void onComplete() {
			}
		};
	}

	private void start(Disposable d) {
		disposable = d;
	}

	private void flush(ReportEntity value) {
		try {
			report = value.getReport();
			final String filePath = getFileNameAsExt(FILE_NAME, new String[] { ".pdf" });

			List<AdditionalDataProcessor> scenarioProcessors = new ArrayList<>();
			scenarioProcessors.add(RestAssuredAdditionalDataProcessor.builder().build());
			ReportData reportData = ExtentPDFReportDataGenerator.builder().scenarioAddDataProcessors(scenarioProcessors)
					.build().generateReportData(report);

			RestAssuredPdfCucumberReport raPdfCucumberReport = new RestAssuredPdfCucumberReport(reportData,
					new File(filePath));
			raPdfCucumberReport.createReport();
		} catch (Exception e) {
			disposable.dispose();
			logger.log(Level.SEVERE, "An exception occurred", e);
		}
	}
}
